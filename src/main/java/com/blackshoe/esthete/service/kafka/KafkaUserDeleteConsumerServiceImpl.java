package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaConsumerDto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.KafkaErrorResult;
import com.blackshoe.esthete.jwt.JWTUtil;
import com.blackshoe.esthete.repository.UserRepository;
import com.blackshoe.esthete.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaUserDeleteConsumerServiceImpl implements KafkaUserDeleteConsumerService{
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @KafkaListener(topics = "user-delete")
    @Transactional
    public void deleteUser(String payload, Acknowledgment acknowledgment) {
        log.info("received payload='{}'", payload);
        KafkaConsumerDto.UserDelete userDelete = null;

        try {
            // 역직렬화
            userDelete = objectMapper.readValue(payload, KafkaConsumerDto.UserDelete.class);
        } catch (Exception e) {
            log.error("Error while converting json string to user object", e);
        }

        log.info("User info : {}", userDelete);

        UUID userId = userDelete.getUserId();
        final User user = userRepository.findByUuid(userId).orElseThrow(() -> new KafkaException(String.valueOf(KafkaErrorResult.USER_NOT_FOUND)));

        userRepository.delete(user);

        //해당 User의 Refresh Token를 지우지 않고 남겨두되 기간이 지나면 redis에서 삭제됨
        //redis에 refresh Token이 존재하더라도 user 정보를 지웠기 때문에 로그인이 되지 않음

        acknowledgment.acknowledge();
    }
}

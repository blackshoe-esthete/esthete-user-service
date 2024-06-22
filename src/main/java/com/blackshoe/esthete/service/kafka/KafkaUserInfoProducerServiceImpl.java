package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaDto;
import com.blackshoe.esthete.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUserInfoProducerServiceImpl implements KafkaUserInfoProducerService{

    private final KafkaProducer kafkaProducer;

    private final ObjectMapper objectMapper;

    @Override
    public void createUser(User user) {
        String filterTopic = "filter-user-create";
        String exhibitionTopic = "exhibition-user-create";

        String filterUserJsonString;
        String exhibitionUserJsonString;

        KafkaDto.FilterUserInfo filterUserInfo = KafkaDto.FilterUserInfo.builder()
                .userId(user.getUuid())
                .nickname(user.getNickname())
                .build();

        KafkaDto.ExhibitionUserInfo exhibitionUserInfo = KafkaDto.ExhibitionUserInfo.builder()
                .userId(user.getUuid())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .build();

        try{
            //직렬화
            filterUserJsonString = objectMapper.writeValueAsString(filterUserInfo);
            exhibitionUserJsonString = objectMapper.writeValueAsString(exhibitionUserInfo);

            kafkaProducer.send(filterTopic, filterUserJsonString);
            kafkaProducer.send(exhibitionTopic, exhibitionUserJsonString);

        } catch (JsonProcessingException e) {
            log.error("Error while converting user object to json string", e);
            throw new RuntimeException("Error while converting user object to json string", e);
        }
    }
}

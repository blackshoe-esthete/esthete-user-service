package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaDto;
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
    public void createUser(KafkaDto.UserInfo userInfo) {
        String topic = "user-create";

        String userJsonString;
        try{
            userJsonString = objectMapper.writeValueAsString(userInfo);
        } catch (JsonProcessingException e) {
            log.error("Error while converting user object to json string", e);
            throw new RuntimeException("Error while converting user object to json string", e);
        }

        kafkaProducer.send("user-info", userInfo.toString());
    }
}

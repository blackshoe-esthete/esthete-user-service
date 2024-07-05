package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaProducerDto;
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
    public void createUser(KafkaProducerDto.UserCreate userCreate) {
        String topic = "user-create";

        String userJsonString;
        try{
            //직렬화
            userJsonString = objectMapper.writeValueAsString(userCreate);
        } catch (JsonProcessingException e) {
            log.error("Error while converting user object to json string", e);
            throw new RuntimeException("Error while converting user object to json string", e);
        }

        kafkaProducer.send(topic, userJsonString);
    }
}

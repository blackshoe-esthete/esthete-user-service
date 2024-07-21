package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaProducerDto;
import com.blackshoe.esthete.entity.User;

public interface KafkaUserInfoProducerService {
    void createUser(KafkaProducerDto.UserCreate userCreate);
}

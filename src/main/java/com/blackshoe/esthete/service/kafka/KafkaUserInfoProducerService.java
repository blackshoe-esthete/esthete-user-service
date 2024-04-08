package com.blackshoe.esthete.service.kafka;

import com.blackshoe.esthete.dto.KafkaDto;

public interface KafkaUserInfoProducerService {
    void createUser(KafkaDto.UserInfo userInfo);
}

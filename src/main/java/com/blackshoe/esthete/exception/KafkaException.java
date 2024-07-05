package com.blackshoe.esthete.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class KafkaException extends RuntimeException{
    private final KafkaErrorResult kafkaErrorResult;

    public KafkaException(KafkaErrorResult kafkaErrorResult) {
        super(kafkaErrorResult.getMessage());
        this.kafkaErrorResult = kafkaErrorResult;
    }
}

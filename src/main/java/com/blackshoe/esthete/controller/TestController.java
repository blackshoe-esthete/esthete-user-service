package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.KafkaDto;
import com.blackshoe.esthete.service.kafka.KafkaUserInfoProducerService;
import com.blackshoe.esthete.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final KafkaUserInfoProducerService kafkaUserInfoProducerService;

    @GetMapping
    public ResponseEntity<?> Test() {

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok("Login user : " + name + "!");
    }

    @PostMapping("/kafka")
    public ResponseEntity<?> TestKafka() {
        KafkaDto.UserInfo userInfo = KafkaDto.UserInfo.builder()
                .name("Kafka Test!")
                .email("kafka@kafka.test").build();
        kafkaUserInfoProducerService.createUser(userInfo);

        return ResponseEntity.ok("Kafka Test!");
    }
    @GetMapping("/gateway")
    public ResponseEntity<ResponseDto> test() {

        Map<String, String> map = Map.of("test", "test");
        ResponseDto responseDto = ResponseDto.builder().payload(map).build();

        return ResponseEntity.ok().body(responseDto);
    }
}

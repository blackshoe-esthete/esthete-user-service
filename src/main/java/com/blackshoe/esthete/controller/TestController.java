package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.KafkaProducerDto;
import com.blackshoe.esthete.entity.Gender;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.service.kafka.KafkaUserInfoProducerService;
import com.blackshoe.esthete.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

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

    @PostMapping("/kafka") //회원가입하면 다 넘겨주는건가? 로그인때마다 넘겨주는 건가?
    public ResponseEntity<?> TestKafka() {
        User user = User.builder()
                .uuid(UUID.fromString("8649fc4b-e22c-4b6f-8b28-22a93c704561"))
                .email("esthete032@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.ofEpochDay(2001-1-1))
                .nickname("Anny")
                .build();

        KafkaProducerDto.UserCreate userCreate = KafkaProducerDto.UserCreate.builder()
                .userId(user.getUuid())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .build();

        kafkaUserInfoProducerService.createUser(userCreate);

        return ResponseEntity.ok("Kafka Test!");
    }
    @GetMapping("/gateway")
    public ResponseEntity<ResponseDto> test() {

        Map<String, String> map = Map.of("test", "test");
        ResponseDto responseDto = ResponseDto.builder().payload(map).build();

        return ResponseEntity.ok().body(responseDto);
    }
}

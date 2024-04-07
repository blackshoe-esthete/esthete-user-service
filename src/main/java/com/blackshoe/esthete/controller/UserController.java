package com.blackshoe.esthete.controller;

import aj.org.objectweb.asm.TypeReference;
import com.blackshoe.esthete.dto.ResponseDto;
import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private String passwordRegex = "^(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$";
    //닉네임 한글 포함 8자리 이하 특수문자X
    //private String nicknameRegex = "^[가-힣a-zA-Z0-9]{1,10}$";


    @PostMapping("/signup/next")
    public ResponseEntity<ResponseDto> joinUser(@RequestBody SignUpDto.ESTSignUpNextRequestDto requestDto) {
        try {
            if (requestDto.getEmail() == null || requestDto.getPassword() == null) {
                System.out.println("계정정보가 누락되었습니다.");
                UserErrorResult userErrorResult = UserErrorResult.REQUIRED_VALUE;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (userService.userExistByEmail(requestDto.getEmail())) {
                System.out.println("이미 존재하는 이메일입니다.");
                UserErrorResult userErrorResult = UserErrorResult.DUPLICATED_EMAIL;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!requestDto.getEmail().matches(emailRegex)) {
                System.out.println("이메일 형식이 아닙니다");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_EMAIL;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!requestDto.getPassword().matches(passwordRegex)) {
                System.out.println("유효하지 않은 비밀번호");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_PASSWORD;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            SignUpDto.ESTSignUpNextResponseDto signUpNextResponseDto = userService.joinUserNext(requestDto);
            ResponseDto responseDto = ResponseDto.builder()
                    .payload(objectMapper.convertValue(signUpNextResponseDto, Map.class))
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            System.out.println("회원가입 다음 단계 진행 불가");
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
    }


}
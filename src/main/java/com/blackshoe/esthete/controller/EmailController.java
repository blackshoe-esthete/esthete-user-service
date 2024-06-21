package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.EmailDto;
import com.blackshoe.esthete.dto.ResponseDto;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.service.EmailSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailSendService mailService;
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    @PostMapping("/validation")
    public ResponseEntity<?> mailSend(@RequestBody @Valid EmailDto.EmailRequestDto requestDto) {
        try {
            if (requestDto.getEmail() == null) {
                System.out.println("이메일이 누락되었습니다.");
                UserErrorResult userErrorResult = UserErrorResult.REQUIRED_VALUE;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!requestDto.getEmail().matches(emailRegex)) {
                System.out.println("이메일 형식이 아닙니다");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_EMAIL;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            System.out.println("이메일 인증 이메일 :" + requestDto.getEmail());
            String authNumber = mailService.joinEmail(requestDto.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body("AuthNumber : " + authNumber);
        }catch (Exception e){
            System.out.println("이메일 전송에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 전송에 실패했습니다.");
        }

    }

    @PostMapping("/verification")
    public ResponseEntity<?> AuthCheck(@RequestBody @Valid EmailDto.EmailCheckDto requestDto){
        if (requestDto.getEmail() == null || requestDto.getAuthNum() == null) {
            System.out.println("필수 정보가 누락되었습니다.");
            UserErrorResult userErrorResult = UserErrorResult.REQUIRED_VALUE;
            ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

            return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
        }

        if (!requestDto.getEmail().matches(emailRegex)) {
            System.out.println("이메일 형식이 아닙니다");
            UserErrorResult userErrorResult = UserErrorResult.INVALID_EMAIL;
            ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

            return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
        }

        if (!requestDto.getAuthNum().matches("\\d{6}")) {
            System.out.println("유효하지 않은 인증번호입니다.");
            UserErrorResult userErrorResult = UserErrorResult.INVALID_AUTH_NUM;
            ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

            return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
        }

        if(mailService.CheckAuthNum(requestDto.getEmail(),requestDto.getAuthNum())){
            return ResponseEntity.status(HttpStatus.OK).body("이메일 인증 성공");
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 인증번호입니다.");
        }
    }
}


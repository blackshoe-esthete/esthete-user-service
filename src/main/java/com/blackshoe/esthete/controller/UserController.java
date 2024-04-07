package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.dto.ResponseDto;
import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/signup/completion")
    public ResponseEntity<ResponseDto> joinUser(@RequestBody SignUpDto.ESTSignUpCompletionRequestDto requestDto){
        try{
            if(requestDto.getNickname() == null || requestDto.getGender() == null || requestDto.getBirthday() == null){
                System.out.println("회원정보가 누락되었습니다.");
                UserErrorResult userErrorResult = UserErrorResult.REQUIRED_VALUE;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!userService.isValidDate(requestDto.getBirthday())) {
                System.out.println("올바르지 않은 생년월일 형식");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_BIRTHDAY;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            SignUpDto.ESTSignUpCompletionResponseDto signUpCompletionResponseDto = userService.joinUserCompletion(requestDto);
            ResponseDto responseDto = ResponseDto.builder()
                    .payload(objectMapper.convertValue(signUpCompletionResponseDto, Map.class))
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }catch (Exception e) {
            System.out.println("회원가입이 정상적으로 진행되지 않음");
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto.ESTLoginRequestDto requestDto) {
        // 로그인 로직 수행
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/social-login")
    public ResponseEntity<ResponseDto> socialLogin(@RequestBody OAuth2Dto.OAuth2RequestDto requestDto, HttpServletResponse response){
        try{
            if (requestDto.getProvider() == null || requestDto.getNickname() == null || requestDto.getEmail() == null) { // gender와 birthday는 필수 값 아닌걸로
                System.out.println("필수 값이 누락되었습니다.");
                UserErrorResult userErrorResult = UserErrorResult.REQUIRED_VALUE;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!userService.isValidDate(requestDto.getBirthday())) {
                System.out.println("올바르지 않은 생년월일 형식");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_BIRTHDAY;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            OAuth2Dto.OAuth2ResponseDto oAuth2ResponseDto = userService.socialLogin(requestDto);
            ResponseDto responseDto = ResponseDto.builder()
                    .payload(objectMapper.convertValue(oAuth2ResponseDto, Map.class))
                    .build();

            //jwt토큰 발급
            Map<String, String> tokens = securityService.saveUserInSecurityContext(requestDto);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            response.addCookie(createCookie(refreshToken));

            return ResponseEntity.ok().headers(headers).body(responseDto);
        }catch (Exception e){
            System.out.println("소셜로그인 실패");
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }

    }


}
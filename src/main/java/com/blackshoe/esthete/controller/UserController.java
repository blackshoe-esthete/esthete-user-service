package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.dto.ResponseDto;
import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.entity.Gender;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.jwt.JWTUtil;
import com.blackshoe.esthete.oauth2.SecurityService;
import com.blackshoe.esthete.service.RedisUtil;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;
    private  final SecurityService securityService;
    private final RedisUtil redisUtil;
    private String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private String passwordRegex = "^(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$";



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

            if (requestDto.getGender().equals(Gender.MALE) || requestDto.getGender().equals(Gender.FEMALE)) {
                System.out.println("성별은 MALE과 FEMALE만 받을 수 있습니다.");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_GENDER;
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

            if (!requestDto.getEmail().matches(emailRegex)) {
                System.out.println("이메일 형식이 아닙니다");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_EMAIL;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (!userService.isValidDate(requestDto.getBirthday())) {
                System.out.println("올바르지 않은 생년월일 형식");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_BIRTHDAY;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            if (requestDto.getProvider().equals("NAVER") || requestDto.getProvider().equals("GOOGLE") || requestDto.getProvider().equals("KAKAO")) {
                System.out.println("유효한 Provider가 아닙니다.");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_PROVIDER;
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

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        try{
            String refresh = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }

            if (refresh == null) {
                //return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
                System.out.println("Refresh 토큰이 비어있습니다");
                UserErrorResult userErrorResult = UserErrorResult.NOT_FOUND_JWT;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

//            //expired check
//            try {
//                jwtUtil.isExpired(refresh);
//            } catch (ExpiredJwtException e) {
//
//                //response status code
//                return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
//            }
            //expired check
            if(jwtUtil.isExpired(refresh)){
                System.out.println("Refresh 토큰이 만료되었습니다.");
                UserErrorResult userErrorResult = UserErrorResult.EXPIRED_JWT;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
            String category = jwtUtil.getCategory(refresh);

            if (!category.equals("refresh")) {
                System.out.println("Refresh 토큰이 아닙니다.");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_JWT;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
                //response status code
                //return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);

            }
            //DB에 저장되어 있는지 확인
            boolean isExist = redisUtil.existsKey(refresh);
            if (!isExist) {
                //return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
                System.out.println("Refresh 토큰을 찾을 수 없습니다.");
                UserErrorResult userErrorResult = UserErrorResult.NOT_FOUND_JWT;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();
                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            String username = jwtUtil.getUsername(refresh);
            String role = jwtUtil.getRole(refresh);

            //make new JWT
            String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
            String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

            //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
            redisUtil.deleteData(refresh);
            redisUtil.setDataExpire(newRefresh, username, 86400000L);

            response.addHeader("Authorization", "Bearer "+ newAccess);
            response.addCookie(createCookie(newRefresh));

            return ResponseEntity.status(HttpStatus.OK).body("Access, Refresh 재발급 성공");
        }catch (Exception e) {
            System.out.println("Access, Refresh 재발급 실패, 다시 요청해주세요.");
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }

    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // logout logic implemented by logoutFilter
        return ResponseEntity.ok("Logged out successfully");
    }


    @PutMapping("/id/check")
    public ResponseEntity<?> checkUserId(@RequestBody LoginDto.FindIDRequestDto requestDto) {
        try{
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

            userService.checkUserId(requestDto);
            return ResponseEntity.status(HttpStatus.OK).body("가입되어 있는 이메일입니다.");

        }catch (Exception e){
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
    }

    @PutMapping("/password/reset")
    public ResponseEntity<ResponseDto> resetPassword(@RequestBody LoginDto.FindPasswordRequestDto requestDto) {
        try{
            if (requestDto.getEmail() == null || requestDto.getNewPassword() == null) {
                System.out.println("필수값이 누락되었습니다.");
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

            if (!requestDto.getNewPassword().matches(passwordRegex)) {
                System.out.println("유효하지 않은 비밀번호입니다.");
                UserErrorResult userErrorResult = UserErrorResult.INVALID_PASSWORD;
                ResponseDto responseDto = ResponseDto.builder().error(userErrorResult.getMessage()).build();

                return ResponseEntity.status(userErrorResult.getHttpStatus()).body(responseDto);
            }

            LoginDto.FindPasswordResponseDto findPasswordResponseDto = userService.resetPassword(requestDto);
            ResponseDto responseDto = ResponseDto.builder()
                    .payload(objectMapper.convertValue(findPasswordResponseDto, Map.class))
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        }catch (Exception e){
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
    }


}
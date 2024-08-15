package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.service.Oauth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;

    /**
     * 소셜 로그인 사용자 정보 조회 API
     * @param provider 소셜 제공자 (google, naver, kakao)
     * @param token 클라이언트에서 전달받은 토큰
     * @return 사용자 정보
     */
    @PostMapping("/userinfo")
    public ResponseEntity<String> getUserInfo(@RequestParam String provider, @RequestParam String token) {
        String userInfo = oauth2Service.getUserInfo(provider, token);

        if (userInfo != null) {
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(401).body("Failed to retrieve user info.");
        }
    }
}

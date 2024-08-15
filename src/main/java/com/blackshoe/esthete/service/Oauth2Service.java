package com.blackshoe.esthete.service;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Oauth2Service {

    // 네이버 API URL
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";
    // 카카오 API URL
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    // 구글 API URL (id_token 기반)
    private static final String GOOGLE_TOKEN_VALIDATION_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 네이버 사용자 정보 조회
     * @param accessToken
     * @return 사용자 정보
     */
    public String getNaverUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(NAVER_USER_INFO_URL, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject userInfo = new JSONObject(response.getBody());
                return userInfo.toString();  // 사용자 정보를 반환
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 카카오 사용자 정보 조회
     * @param accessToken
     * @return 사용자 정보
     */
    public String getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject userInfo = new JSONObject(response.getBody());
                return userInfo.toString();  // 사용자 정보를 반환
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 구글 사용자 정보 조회 (id_token 디코딩)
     * @param idToken
     * @return 사용자 정보
     */
    public String getGoogleUserInfo(String idToken) {
        String url = GOOGLE_TOKEN_VALIDATION_URL + idToken;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject userInfo = new JSONObject(response.getBody());
                return userInfo.toString();  // 사용자 정보를 반환
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 통합 사용자 정보 조회 메서드
     * @param provider 소셜 제공자 (google, naver, kakao)
     * @param token 해당 플랫폼의 액세스 또는 id 토큰
     * @return 사용자 정보
     */
    public String getUserInfo(String provider, String token) {
        switch (provider.toLowerCase()) {
            case "google":
                return getGoogleUserInfo(token);
            case "naver":
                return getNaverUserInfo(token);
            case "kakao":
                return getKakaoUserInfo(token);
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
}
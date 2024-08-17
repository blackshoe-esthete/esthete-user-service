package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.OAuth2Dto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface SecurityService {
//    Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2CheckDto requestDto);
//    Map<String, String> saveUserInSecurityContextForOAuthSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto);
//    Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider);
//    UserDetails loadUserBySocialIdAndSocialProvider(String socialId, String socialProvider);

    // 소셜 로그인 처리 (카카오, 네이버 등 소셜 ID 기반)
    Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2CheckDto requestDto);

    // 소셜 회원가입 시 이메일을 식별자로 사용하는 경우 (Google, 혹은 공통 이메일)
    Map<String, String> saveUserInSecurityContextForOAuthSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto);

    // 소셜 ID를 기반으로 JWT 발급 (카카오 ID, 네이버 ID)
    Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider);

    // 이메일을 기반으로 JWT 발급 (Google, 혹은 이메일 기반 소셜 로그인)
    Map<String, String> saveUserInSecurityContextByEmail(String email, String socialProvider);

    // 소셜 ID로 사용자 정보를 조회
    UserDetails loadUserBySocialIdAndProvider(String socialId, String socialProvider);

    // 이메일로 사용자 정보를 조회
    UserDetails loadUserByEmailAndProvider(String email, String socialProvider);

}

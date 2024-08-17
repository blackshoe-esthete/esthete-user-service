package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.jwt.JWTUtil;
import com.blackshoe.esthete.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RedisService redisUtil;

    @Value("${myapp.access.expiration}")
    private Long accessExpiration;

    @Value("${myapp.refresh.expiration}")
    private Long refreshExpiration;


//    public Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2CheckDto requestDto) {
//        String socialId = requestDto.getEmail();
//        String socialProvider = requestDto.getProvider();
//        return saveUserInSecurityContext(socialId, socialProvider);
//    }
//
//    public Map<String, String> saveUserInSecurityContextForOAuthSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto) {
//        String socialId = requestDto.getEmail();
//        String socialProvider = requestDto.getProvider();
//        return saveUserInSecurityContext(socialId, socialProvider);
//    }
//
//    public Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider) { //jwt 발급
//        UserDetails userDetails = loadUserBySocialIdAndSocialProvider(socialId, socialProvider); //ori
//
//        String username = userDetails.getUsername();
//        System.out.println("successful함수" + username);
//
//        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities(); //ori
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); //한 유저당 여러 역할이 있을 수 있으니 iterator사용
//        GrantedAuthority auth = iterator.next(); // role들은 GrantedAuthority에서 항상 관리하는듯
//        System.out.println("successful함수2" + username);
//
//        String role = auth.getAuthority();
//        User findUser = userRepository.findByEmail(username).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
//        UUID userId = findUser.getUuid();
//
//        String access = jwtUtil.createJwt("access",username, userId, role, accessExpiration);
//        String refresh = jwtUtil.createJwt("refresh",username, userId, role, refreshExpiration);
//
//        redisUtil.setDataExpire(refresh, username, refreshExpiration);
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);//ori
//
//        if(authentication != null) {
//            SecurityContext context = SecurityContextHolder.createEmptyContext();
//            context.setAuthentication(authentication);
//            SecurityContextHolder.setContext(context);
//        }
//        // 토큰들을 Map에 담아 반환
//        Map<String, String> tokens = new HashMap<>();
//        tokens.put("accessToken", access);
//        tokens.put("refreshToken", refresh);
//
//        return tokens;
//    }
//
//    public UserDetails loadUserBySocialIdAndSocialProvider(String socialId, String socialProvider) {
//        User user = userRepository.findByEmailAndProvider(socialId, socialProvider).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//        return userDetails;
//    }

    // OAuth2CheckDto에서 소셜 제공자에 따라 적절한 식별자를 사용
    public Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2CheckDto requestDto) {
        String socialId = null;
        String socialProvider = requestDto.getProvider();

        if ("kakao".equals(socialProvider)) {
            socialId = requestDto.getKakaoId();  // kakao는 kakaoId를 식별자로 사용
        } else if ("naver".equals(socialProvider)) {
            socialId = requestDto.getNaverId();  // naver는 naverId를 식별자로 사용
        } else if ("google".equals(socialProvider)) {
            socialId = requestDto.getEmail();  // google은 email을 식별자로 사용
        }

        if (socialId == null || socialProvider == null) {
            throw new IllegalArgumentException("유효하지 않은 소셜 로그인 정보입니다.");
        }

        return saveUserInSecurityContext(socialId, socialProvider);
    }

    // OAuth2SignUpRequestDto에서 모든 소셜 제공자에 대해 이메일을 공통 식별자로 사용
    public Map<String, String> saveUserInSecurityContextForOAuthSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto) {
        String email = requestDto.getEmail();  // 이메일을 식별자로 사용
        String socialProvider = requestDto.getProvider();
        return saveUserInSecurityContextByEmail(email, socialProvider);
    }

    // 소셜 아이디를 사용한 경우 (kakaoId, naverId 등)
    public Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider) {
        UserDetails userDetails = loadUserBySocialIdAndProvider(socialId, socialProvider);  // 소셜 ID로 로드

        return generateTokensAndSetSecurityContext(userDetails);
    }

    // 이메일을 사용한 경우 (Google, 혹은 공통 이메일)
    public Map<String, String> saveUserInSecurityContextByEmail(String email, String socialProvider) {
        UserDetails userDetails = loadUserByEmailAndProvider(email, socialProvider);  // 이메일로 로드

        return generateTokensAndSetSecurityContext(userDetails);
    }

    // 소셜 ID로 유저 로드 (kakaoId, naverId 등)
    public UserDetails loadUserBySocialIdAndProvider(String socialId, String socialProvider) {
        Optional<User> user;

        if ("kakao".equals(socialProvider)) {
            user = userRepository.findByKakaoIdAndProvider(socialId, socialProvider);
        } else if ("naver".equals(socialProvider)) {
            user = userRepository.findByNaverIdAndProvider(socialId, socialProvider);
        } else if ("google".equals(socialProvider)){
            user = userRepository.findByEmailAndProvider(socialId, socialProvider);
        } else {
            throw new IllegalArgumentException("유효하지 않은 소셜 제공자입니다.");
        }

        return new CustomUserDetails(user.orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER)));
    }

    // 이메일로 유저 로드 (google 등)
    public UserDetails loadUserByEmailAndProvider(String email, String socialProvider) {
        User user = userRepository.findByEmailAndProvider(email, socialProvider)
                .orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));

        return new CustomUserDetails(user);
    }

    // JWT 생성 및 SecurityContext에 사용자 정보 설정
    private Map<String, String> generateTokensAndSetSecurityContext(UserDetails userDetails) {
        String username = userDetails.getUsername();
        System.out.println("successful함수" + username);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();
        User findUser = userRepository.findByEmail(username).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
        UUID userId = findUser.getUuid();

        String access = jwtUtil.createJwt("access", username, userId, role, accessExpiration);
        String refresh = jwtUtil.createJwt("refresh", username, userId, role, refreshExpiration);

        redisUtil.setDataExpire(refresh, username, refreshExpiration);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        if (authentication != null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);

        return tokens;
    }

}

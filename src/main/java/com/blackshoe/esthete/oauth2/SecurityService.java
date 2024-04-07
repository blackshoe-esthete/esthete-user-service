package com.blackshoe.esthete.oauth2;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.TokenErrorResult;
import com.blackshoe.esthete.exception.TokenException;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.jwt.JWTUtil;
import com.blackshoe.esthete.repository.UserRepository;
import com.blackshoe.esthete.service.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SecurityService {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;


    public Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2RequestDto requestDto) {
        String socialId = requestDto.getEmail();
        String socialProvider = requestDto.getProvider();
        return saveUserInSecurityContext(socialId, socialProvider);
    }

    private Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider) { //jwt 발급
        UserDetails userDetails = loadUserBySocialIdAndSocialProvider(socialId, socialProvider); //ori

        String username = userDetails.getUsername();
        System.out.println("successful함수" + username);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities(); //ori
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); //한 유저당 여러 역할이 있을 수 있으니 iterator사용
        GrantedAuthority auth = iterator.next(); // role들은 GrantedAuthority에서 항상 관리하는듯
        System.out.println("successful함수2" + username);

        String role = auth.getAuthority();
        String access = jwtUtil.createJwt("access",username, role, 60*60*10L);
        String refresh = jwtUtil.createJwt("refresh",username, role, 60*60*10L);

        redisUtil.setDataExpire(refresh, username, 86400000L);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);//ori

        if(authentication != null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        // 토큰들을 Map에 담아 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);

        return tokens;
    }

    private UserDetails loadUserBySocialIdAndSocialProvider(String socialId, String socialProvider) {
        User user = userRepository.findByEmailAndProvider(socialId, socialProvider).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
//        if (user == null) {
//            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
//        } else {
//            CustomUserDetails userDetails = new CustomUserDetails(user);
//            return userDetails;
//        }
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return userDetails;
    }


}
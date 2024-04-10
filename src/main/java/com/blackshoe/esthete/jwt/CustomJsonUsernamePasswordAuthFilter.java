package com.blackshoe.esthete.jwt;//package com.blackshoe.esthete.jwt;


import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.service.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomJsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;

    private final RedisUtil redisUtil;

//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        UsernamePasswordAuthenticationToken authenticationToken;
//        if(request.getContentType().equals(MimeTypeUtils.APPLICATION_JSON_VALUE)){
//            try{
//                LoginDto.ESTLoginRequestDto requestDto = objectMapper.readValue(request.getReader().lines().collect(Collectors.joining()), LoginDto.ESTLoginRequestDto.class);
//                System.out.println(requestDto.getUsername());
//                authenticationToken = new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword());
//                System.out.println("JSON 변환 된 거 같은, try문");
//            }catch (IOException e) {
//                //e.printStackTrace();
//                throw new AuthenticationServiceException("Failed to read JSON request body", e);
//            }
//        }else{//form-request
//            String username = obtainUsername(request);
//            System.out.println("LoginFilter : "+username);
//            String password = obtainPassword(request);
//            System.out.println("LoginFilter : "+password);
//            authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
//        }
//        this.setDetails(request, authenticationToken);
//        return this.getAuthenticationManager().authenticate(authenticationToken);
//    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authenticationToken;
        try {
            if (request.getContentType().equals(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
                try {
                    LoginDto.ESTLoginRequestDto requestDto = objectMapper.readValue(request.getReader().lines().collect(Collectors.joining()), LoginDto.ESTLoginRequestDto.class);
                    System.out.println(requestDto.getUsername());
                    authenticationToken = new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword());
                    System.out.println("JSON 변환 된 거 같은, try문");
                } catch (IOException e) {
                    throw new AuthenticationServiceException("Failed to read JSON request body", e);
                }
            } else { // form-request
                String username = obtainUsername(request);
                System.out.println("LoginFilter : " + username);
                String password = obtainPassword(request);
                System.out.println("LoginFilter : " + password);
                authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            }
            this.setDetails(request, authenticationToken);
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            writer.print(e.getMessage());
        }
        return null;
    }



    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException{
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();//authentication에 있는 user정보를 가져오는것

        String username = customUserDetails.getUsername();
        System.out.println("successful함수" + username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); //한 유저당 여러 역할이 있을 수 있으니 iterator사용
        GrantedAuthority auth = iterator.next(); // role들은 GrantedAuthority에서 항상 관리하는듯

        System.out.println("successful함수2" + username);

        String role = auth.getAuthority();

        String access = jwtUtil.createJwt("access",username, role, 60*60*10L); //첫 로그인이든 아니든 새로운 토큰 생성 후 반환
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Refresh 토큰 저장
        redisUtil.setDataExpire(refresh, username, 86400000L);

        response.addHeader("Authorization", "Bearer "+ access); // 인가 받은 사용자, Bearer뒤에 반드시 공백하나
        //response.setHeader("access", access);
        response.addCookie(createCookie(refresh));
        response.setStatus(HttpStatus.OK.value());
        System.out.println("success");

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.print("로그인이 완료되었습니다.");
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException{
        //로그인 실패시 401 응답 코드 반환
        System.out.println("fail");
        response.setStatus(401);
    }

    private Cookie createCookie(String value) {

        Cookie cookie = new Cookie("refresh", value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}


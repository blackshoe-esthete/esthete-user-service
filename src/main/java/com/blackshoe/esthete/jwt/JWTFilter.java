package com.blackshoe.esthete.jwt;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.entity.Role;
import com.blackshoe.esthete.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        //String accessToken = request.getHeader("access");
        System.out.println("request: "+request.getMethod());


        // 토큰이 없다면 다음 필터로 넘김
        if (authorization == null || !authorization.startsWith("Bearer ")) {//if (accessToken == null) {
            System.out.println("token null");
            filterChain.doFilter(request, response);

            return;
        }

        System.out.println("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String accessToken = authorization.split(" ")[1];

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);
        System.out.println("category : " + category);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        //3. 정상적인 토큰인 경우

        String username = jwtUtil.getUsername(accessToken);
        System.out.println("username : " + username);
        String role = jwtUtil.getRole(accessToken);

        //token에서 뽑아낸 정보들로 객체를 만듦
//        LoginDto.IDPWDto idpwDto = LoginDto.IDPWDto.builder()
//                .email(username)
//                .password("tempPW")
//                .role(role)
//                .build();
        User userEntity = User.builder()
                .email(username)
                .role(Role.valueOf(role))
                .build();

        //뽑아낸 객체를 customUSerDetails에 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities()); // 다음 필터에서 사용자 role을 알 수 있도록 넘겨줌

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);


    }
}
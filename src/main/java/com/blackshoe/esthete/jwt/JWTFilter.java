package com.blackshoe.esthete.jwt;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.entity.Role;
import com.blackshoe.esthete.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        System.out.println("request: "+request.getMethod());

        // 토큰이 없다면 다음 필터로 넘김
        if (authorization == null || !authorization.startsWith("Bearer ")) {
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
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.print("AccessToken이 만료되었습니다.");
            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);
        System.out.println("category : " + category);

        if (!category.equals("access")) {
            //response body
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.print("유효하지 않은 AccessToken입니다.");
            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //3. 정상적인 토큰인 경우
        String username = jwtUtil.getUsername(accessToken);
        System.out.println("username : " + username);
        String role = jwtUtil.getRole(accessToken);

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
package com.blackshoe.esthete.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    private final ObjectMapper objectMapper;


    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws  Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception {
        // Swagger 관련 URI 패턴
        String[] SWAGGER_URI = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.index.html",
                "/webjars/**",
                "/swagger-resources/**"
        };

        //로그인 단에서 발생하는 cors 문제 해결 방법
        http.cors((corCustomizer -> corCustomizer.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // CorsConfiguration에서 원격 주소에서 오는 요청 허용하기
                configuration.setAllowedMethods(Collections.singletonList("*")); //모든 CRUD 요청을 허용한다.
                configuration.setAllowCredentials(true); // 보장되었다?
                configuration.setAllowedHeaders(Collections.singletonList("*")); // header에 값들은 모두 허용한다?
                configuration.setMaxAge(3600L);
                configuration.setExposedHeaders(Collections.singletonList("Authorization")); // token이 들어있는 header부분을 허용한다.
                return configuration;
            }
        })));

        //csrf disable
        http.csrf(AbstractHttpConfigurer::disable);
        //http.csrf(AbstractHttpConfigurer::disable);

        //Form 로그인 방식 disable
        http.formLogin(AbstractHttpConfigurer::disable);
        //http.formLogin(AbstractHttpConfigurer::disable);

        //http basic 인증 방식 disable
        http.httpBasic(AbstractHttpConfigurer::disable);
        //http.httpBasic(AbstractHttpConfigurer::disable);


        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(SWAGGER_URI).permitAll()
                .requestMatchers("/login", "/", "users/signup/next", "users/signup/completion"
                        ,"/users/signup/email/validation", "/users/signup/email/verification"
                        , "/reissue","/social-login").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated());
//        //JWTFilter 등록
//        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class); //로그인 전에 JWT token을 검증하는 과정
//
//        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
//        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, objectMapper), UsernamePasswordAuthenticationFilter.class);

//        http.addFilterBefore(new JWTFilter(jwtUtil), CustomJsonUsernamePasswordAuthFilter.class); //로그인 전에 JWT token을 검증하는 과정
//
//        http.addFilterAt(new CustomJsonUsernamePasswordAuthFilter(objectMapper, jwtUtil), UsernamePasswordAuthenticationFilter.class);


        return http.build();

    }
}
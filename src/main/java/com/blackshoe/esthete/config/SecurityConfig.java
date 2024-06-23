package com.blackshoe.esthete.config;

//import com.blackshoe.esthete.jwt.CustomJsonUsernamePasswordAuthFilter;
import com.blackshoe.esthete.jwt.CustomJsonUsernamePasswordAuthFilter;
import com.blackshoe.esthete.jwt.CustomLogoutFilter;
import com.blackshoe.esthete.jwt.JWTFilter;
import com.blackshoe.esthete.jwt.JWTUtil;
//import com.blackshoe.esthete.jwt.LoginFilter;
import com.blackshoe.esthete.repository.UserRepository;
import com.blackshoe.esthete.service.RedisService;
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

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RedisService redisUtil;
    private final UserRepository userRepository;

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
                .requestMatchers("/login", "/", "/signup/next", "/signup/completion"
                        ,"/signup/email/validation", "/signup/email/verification"
                        , "/reissue","/social-login", "/id/check", "/password/reset", "/test/gateway","/test/kafka").permitAll() //reissue제외
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated());

        http.addFilterBefore(new JWTFilter(jwtUtil), CustomJsonUsernamePasswordAuthFilter.class); //로그인 전에 JWT token을 검증하는 과정
        http.addFilterAt(getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, redisUtil), LogoutFilter.class);

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();

    }

    @Bean
    protected CustomJsonUsernamePasswordAuthFilter getAuthenticationFilter(){
        CustomJsonUsernamePasswordAuthFilter authFilter = new CustomJsonUsernamePasswordAuthFilter(objectMapper, jwtUtil, redisUtil, userRepository);
        try{
            authFilter.setFilterProcessesUrl("/login");
            authFilter.setAuthenticationManager(this.authenticationManager(authenticationConfiguration));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return authFilter;
    }
}

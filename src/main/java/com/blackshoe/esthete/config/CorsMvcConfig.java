package com.blackshoe.esthete.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    // Mvc 패턴으로 구현된 Controller등에 접근하기 위해서 따로 구현
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:3000"); // 해당 주소에서 Mvc 패턴 controller에 구현한 모든 경로로 요청 보낼 수 있도록 허용
    }
}

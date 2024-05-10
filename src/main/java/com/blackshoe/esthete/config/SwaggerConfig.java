package com.blackshoe.esthete.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info().title("Esthete User Service API")
                        .description("Esthete User Service LIST")
                        .version("0.0.1"))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Arrays.asList(securityRequirement))
                .servers(Arrays.asList(
                        //new io.swagger.v3.oas.models.servers.Server().url("https://user-api.esthete.com").description("Production server"),
                        new io.swagger.v3.oas.models.servers.Server().url("http://43.201.228.22:8020").description("Test Server"),
                        new io.swagger.v3.oas.models.servers.Server().url("http://localhost:8020").description("Local development server")
                ));
    }
}
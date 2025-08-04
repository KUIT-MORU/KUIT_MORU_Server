package com.moru.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                // "/" 로 설정하면,
                // → swagger-ui 가 열려 있는 호스트(=자기자신) 기준으로 경로를 구성합니다.
                @Server(url = "/", description = "Current host (relative)")
        }
)
@Configuration
public class OpenApiConfig { }

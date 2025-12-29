package com.perpustakaan.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Perpustakaan API Gateway") // Judul Utama
                .version("1.0.0")
                .description("Pintu masuk utama (Centralized Documentation) untuk semua layanan perpustakaan.")
                .contact(new Contact()
                    .name("Tim DevOps Perpustakaan")
                    .email("admin@perpustakaan.com")))
            .servers(List.of(
                // Di Gateway, servernya HANYA diri dia sendiri (Port 8080)
                new Server().url("http://localhost:8080").description("API Gateway URL")
            ));
    }
}
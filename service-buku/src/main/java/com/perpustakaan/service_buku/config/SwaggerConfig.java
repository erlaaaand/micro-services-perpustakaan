package com.perpustakaan.service_buku.config;

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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Buku API")
                        .version("1.0.0")
                        .description("REST API for Library Book Management")
                        .contact(new Contact()
                                .name("Perpustakaan Team")
                                .email("team@perpustakaan.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Via Gateway")
                ));
    }
}
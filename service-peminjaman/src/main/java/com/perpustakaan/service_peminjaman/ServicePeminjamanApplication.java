package com.perpustakaan.service_peminjaman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.perpustakaan.service_peminjaman.repository.command")
@EnableMongoRepositories(basePackages = "com.perpustakaan.service_peminjaman.repository.query")
public class ServicePeminjamanApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicePeminjamanApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
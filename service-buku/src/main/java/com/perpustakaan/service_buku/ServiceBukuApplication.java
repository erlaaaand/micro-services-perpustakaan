package com.perpustakaan.service_buku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaRepositories(basePackages = "com.perpustakaan.service_buku.repository.command")
@EnableMongoRepositories(basePackages = "com.perpustakaan.service_buku.repository.query")
@SpringBootApplication
@EnableDiscoveryClient // Tambahkan
public class ServiceBukuApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceBukuApplication.class, args);
	}
}

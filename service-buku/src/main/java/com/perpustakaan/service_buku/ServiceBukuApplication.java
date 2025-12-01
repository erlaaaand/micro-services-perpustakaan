package com.perpustakaan.service_buku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // Import

@SpringBootApplication
@EnableDiscoveryClient // Tambahkan
public class ServiceBukuApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceBukuApplication.class, args);
	}
}
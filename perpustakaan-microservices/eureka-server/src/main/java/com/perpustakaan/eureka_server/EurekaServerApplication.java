package com.perpustakaan.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer; // 1. Pastikan baris ini ada

@SpringBootApplication
@EnableEurekaServer // 2. Tambahkan anotasi ini agar aktif
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
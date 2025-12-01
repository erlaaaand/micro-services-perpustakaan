package com.perpustakaan.service_anggota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // Import

@SpringBootApplication
@EnableDiscoveryClient // Tambahkan ini
public class ServiceAnggotaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAnggotaApplication.class, args);
	}

}
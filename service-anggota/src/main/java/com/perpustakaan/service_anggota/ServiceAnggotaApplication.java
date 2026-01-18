package com.perpustakaan.service_anggota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableDiscoveryClient
// 1. Beritahu di mana JPA Repository berada
@EnableJpaRepositories(basePackages = "com.perpustakaan.service_anggota.repository.command")
// 2. Beritahu di mana Mongo Repository berada
@EnableMongoRepositories(basePackages = "com.perpustakaan.service_anggota.repository.query")
public class ServiceAnggotaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAnggotaApplication.class, args);
    }
}
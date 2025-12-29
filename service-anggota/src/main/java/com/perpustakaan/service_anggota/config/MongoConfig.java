package com.perpustakaan.service_anggota.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.perpustakaan.service_anggota.repository.query" // Hanya scan folder repository query
)
public class MongoConfig {
    // Spring Boot otomatis mengkonfigurasi MongoTemplate & Embedded Mongo
}
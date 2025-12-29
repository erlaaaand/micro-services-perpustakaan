package com.perpustakaan.service_buku.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.perpustakaan.service_buku.repository.query" // Hanya scan folder repository query
)
public class MongoConfig {
    // Spring Boot otomatis mengkonfigurasi MongoTemplate & Embedded Mongo
}
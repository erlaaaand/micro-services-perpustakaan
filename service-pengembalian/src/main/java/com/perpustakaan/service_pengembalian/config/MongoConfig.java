package com.perpustakaan.service_pengembalian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.perpustakaan.service_pengembalian.repository.query" // Scan Mongo Repo
)
public class MongoConfig {
}
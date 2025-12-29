package com.perpustakaan.service_peminjaman.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.perpustakaan.service_peminjaman.repository.query" // Folder baru untuk Mongo Repo
)
public class MongoConfig {
}
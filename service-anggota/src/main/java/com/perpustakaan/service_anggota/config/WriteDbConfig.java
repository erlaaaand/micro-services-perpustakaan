package com.perpustakaan.service_anggota.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.perpustakaan.service_anggota.repository.command",
    entityManagerFactoryRef = "writeEntityManagerFactory",
    transactionManagerRef = "writeTransactionManager"
)
public class WriteDbConfig {

    @Primary
    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "writeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("writeDataSource") DataSource dataSource) {
        
        // --- TAMBAHAN PENTING: Konfigurasi Hibernate Manual ---
        Map<String, Object> properties = new HashMap<>();
        
        // 1. Memaksa Hibernate membuat tabel otomatis (Create/Update)
        properties.put("hibernate.hbm2ddl.auto", "update");
        
        // 2. Mengatur strategi penamaan (Anggota -> anggota)
        properties.put("hibernate.physical_naming_strategy", 
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        
        // 3. (Opsional) Menampilkan SQL di log
        properties.put("hibernate.show_sql", "true");

        return builder
                .dataSource(dataSource)
                .packages("com.perpustakaan.service_anggota.entity.command")
                .persistenceUnit("writePU")
                .properties(properties) // <--- Inject properties di sini
                .build();
    }

    @Primary
    @Bean(name = "writeTransactionManager")
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
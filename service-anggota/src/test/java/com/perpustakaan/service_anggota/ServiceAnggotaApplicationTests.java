package com.perpustakaan.service_anggota;

import com.perpustakaan.service_anggota.controller.AnggotaController;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import com.perpustakaan.service_anggota.service.AnggotaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ServiceAnggotaApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private AnggotaController anggotaController;

    @Autowired(required = false)
    private AnggotaService anggotaService;

    @Autowired(required = false)
    private AnggotaRepository anggotaRepository;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("All required beans should be loaded")
    void testRequiredBeansAreLoaded() {
        assertThat(anggotaController).isNotNull();
        assertThat(anggotaService).isNotNull();
        assertThat(anggotaRepository).isNotNull();
    }

    @Test
    @DisplayName("Controller bean should be available")
    void testControllerBeanExists() {
        assertThat(applicationContext.containsBean("anggotaController")).isTrue();
    }

    @Test
    @DisplayName("Service bean should be available")
    void testServiceBeanExists() {
        assertThat(applicationContext.getBean(AnggotaService.class)).isNotNull();
    }

    @Test
    @DisplayName("Repository bean should be available")
    void testRepositoryBeanExists() {
        assertThat(applicationContext.getBean(AnggotaRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Application should have correct profile active")
    void testActiveProfile() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        assertThat(activeProfiles).contains("test");
    }

    @Test
    @DisplayName("H2 database should be configured")
    void testDatabaseConfiguration() {
        String dataSourceUrl = applicationContext.getEnvironment()
            .getProperty("spring.datasource.url");
        assertThat(dataSourceUrl).contains("h2");
        assertThat(dataSourceUrl).contains("mem");
    }

    @Test
    @DisplayName("Application name should be configured")
    void testApplicationName() {
        String appName = applicationContext.getEnvironment()
            .getProperty("spring.application.name");
        assertThat(appName).isNotNull();
        assertThat(appName).contains("anggota");
    }
}
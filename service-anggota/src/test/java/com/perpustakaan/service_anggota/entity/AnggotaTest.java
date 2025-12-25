package com.perpustakaan.service_anggota.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnggotaTest {

    @Test
    @DisplayName("Should create Anggota with no-args constructor")
    void testNoArgsConstructor() {
        Anggota anggota = new Anggota();
        assertThat(anggota).isNotNull();
    }

    @Test
    @DisplayName("Should create Anggota with all-args constructor")
    void testAllArgsConstructor() {
        Anggota anggota = new Anggota(1L, "A001", "John Doe", "Jl. Test", "john@test.com");

        assertThat(anggota.getId()).isEqualTo(1L);
        assertThat(anggota.getNomorAnggota()).isEqualTo("A001");
        assertThat(anggota.getNama()).isEqualTo("John Doe");
        assertThat(anggota.getAlamat()).isEqualTo("Jl. Test");
        assertThat(anggota.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("Should set and get all fields")
    void testGettersAndSetters() {
        Anggota anggota = new Anggota();
        
        anggota.setId(1L);
        anggota.setNomorAnggota("A001");
        anggota.setNama("John Doe");
        anggota.setAlamat("Jl. Test No. 123");
        anggota.setEmail("john@test.com");

        assertThat(anggota.getId()).isEqualTo(1L);
        assertThat(anggota.getNomorAnggota()).isEqualTo("A001");
        assertThat(anggota.getNama()).isEqualTo("John Doe");
        assertThat(anggota.getAlamat()).isEqualTo("Jl. Test No. 123");
        assertThat(anggota.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Anggota anggota1 = new Anggota(1L, "A001", "John", "Jl. A", "john@test.com");
        Anggota anggota2 = new Anggota(1L, "A001", "John", "Jl. A", "john@test.com");
        Anggota anggota3 = new Anggota(2L, "A002", "Jane", "Jl. B", "jane@test.com");

        assertThat(anggota1).isEqualTo(anggota2);
        assertThat(anggota1).isNotEqualTo(anggota3);
        assertThat(anggota1.hashCode()).isEqualTo(anggota2.hashCode());
    }

    @Test
    @DisplayName("Should generate toString correctly")
    void testToString() {
        Anggota anggota = new Anggota(1L, "A001", "John Doe", "Jl. Test", "john@test.com");
        String toString = anggota.toString();

        assertThat(toString).contains("A001");
        assertThat(toString).contains("John Doe");
        assertThat(toString).contains("john@test.com");
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        Anggota anggota = new Anggota();
        
        assertThat(anggota.getId()).isNull();
        assertThat(anggota.getNomorAnggota()).isNull();
        assertThat(anggota.getNama()).isNull();
        assertThat(anggota.getAlamat()).isNull();
        assertThat(anggota.getEmail()).isNull();
    }
}
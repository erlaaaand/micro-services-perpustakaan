package com.perpustakaan.service_anggota.repository;

import com.perpustakaan.service_anggota.entity.Anggota;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Memuat full application context (lebih aman jika konfigurasi slice test bermasalah)
@ActiveProfiles("test") // Menggunakan konfigurasi dari application-test.properties (H2 DB)
@Transactional // Rollback transaksi setelah setiap test selesai agar data bersih
class AnggotaRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AnggotaRepository anggotaRepository;

    private Anggota testAnggota;

    @BeforeEach
    void setUp() {
        // Membersihkan database untuk memastikan isolasi test
        anggotaRepository.deleteAll();

        // Setup data dummy
        testAnggota = new Anggota();
        testAnggota.setNomorAnggota("A001");
        testAnggota.setNama("John Doe");
        testAnggota.setAlamat("Jl. Test No. 123");
        testAnggota.setEmail("john@test.com");
    }

    @Test
    @DisplayName("Should save anggota successfully")
    void testSaveAnggota() {
        // When
        Anggota saved = anggotaRepository.save(testAnggota);
        entityManager.flush(); // Paksa sinkronisasi ke DB

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNomorAnggota()).isEqualTo("A001");
    }

    @Test
    @DisplayName("Should find anggota by ID")
    void testFindById() {
        // Given
        entityManager.persist(testAnggota);
        entityManager.flush();
        // Clear persistence context untuk memastikan data diambil dari DB, bukan cache session
        entityManager.clear();

        // When
        Optional<Anggota> found = anggotaRepository.findById(testAnggota.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNomorAnggota()).isEqualTo("A001");
        assertThat(found.get().getNama()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should find anggota by Nomor Anggota")
    void testFindByNomorAnggota() {
        // Given
        entityManager.persist(testAnggota);
        entityManager.flush();

        // When
        Anggota found = anggotaRepository.findByNomorAnggota("A001");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(testAnggota.getId());
    }

    @Test
    @DisplayName("Should find all anggota")
    void testFindAll() {
        // Given
        Anggota a1 = new Anggota(null, "A002", "Jane", "Jl B", "jane@test.com");
        Anggota a2 = new Anggota(null, "A003", "Bob", "Jl C", "bob@test.com");

        // Simpan data testAnggota (dari setUp) + 2 data baru
        entityManager.persist(testAnggota);
        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.flush();

        // When
        List<Anggota> all = anggotaRepository.findAll();

        // Then
        assertThat(all).hasSize(3); // A001, A002, A003
    }

    @Test
    @DisplayName("Should delete anggota by ID")
    void testDeleteById() {
        // Given
        Anggota saved = anggotaRepository.save(testAnggota);
        entityManager.flush();
        Long id = saved.getId();

        // When
        anggotaRepository.deleteById(id);
        entityManager.flush();

        // Then
        Optional<Anggota> deleted = anggotaRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}
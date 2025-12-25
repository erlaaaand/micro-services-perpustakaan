package com.perpustakaan.service_anggota.repository;

import com.perpustakaan.service_anggota.entity.Anggota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AnggotaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AnggotaRepository anggotaRepository;

    private Anggota testAnggota;

    @BeforeEach
    void setUp() {
        testAnggota = new Anggota();
        testAnggota.setNomorAnggota("A001");
        testAnggota.setNama("John Doe");
        testAnggota.setAlamat("Jl. Test No. 123");
        testAnggota.setEmail("john@test.com");
    }

    @Test
    @DisplayName("Should save anggota successfully")
    void testSaveAnggota() {
        Anggota saved = anggotaRepository.save(testAnggota);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNomorAnggota()).isEqualTo("A001");
        assertThat(saved.getNama()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should find anggota by id")
    void testFindById() {
        Anggota saved = entityManager.persistAndFlush(testAnggota);

        Optional<Anggota> found = anggotaRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNomorAnggota()).isEqualTo("A001");
    }

    @Test
    @DisplayName("Should find anggota by nomor anggota")
    void testFindByNomorAnggota() {
        entityManager.persistAndFlush(testAnggota);

        Anggota found = anggotaRepository.findByNomorAnggota("A001");

        assertThat(found).isNotNull();
        assertThat(found.getNama()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return null when nomor anggota not found")
    void testFindByNomorAnggota_NotFound() {
        Anggota found = anggotaRepository.findByNomorAnggota("NOT_EXISTS");

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should find all anggota")
    void testFindAll() {
        Anggota anggota2 = new Anggota();
        anggota2.setNomorAnggota("A002");
        anggota2.setNama("Jane Doe");
        anggota2.setAlamat("Jl. Test No. 456");
        anggota2.setEmail("jane@test.com");

        entityManager.persist(testAnggota);
        entityManager.persist(anggota2);
        entityManager.flush();

        List<Anggota> anggotaList = anggotaRepository.findAll();

        assertThat(anggotaList).hasSize(2);
        assertThat(anggotaList).extracting(Anggota::getNama)
            .containsExactlyInAnyOrder("John Doe", "Jane Doe");
    }

    @Test
    @DisplayName("Should update anggota")
    void testUpdateAnggota() {
        Anggota saved = entityManager.persistAndFlush(testAnggota);

        saved.setNama("John Updated");
        saved.setEmail("updated@test.com");
        
        Anggota updated = anggotaRepository.save(saved);
        entityManager.flush();

        Anggota found = entityManager.find(Anggota.class, updated.getId());
        assertThat(found.getNama()).isEqualTo("John Updated");
        assertThat(found.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("Should delete anggota by id")
    void testDeleteById() {
        Anggota saved = entityManager.persistAndFlush(testAnggota);
        Long id = saved.getId();

        anggotaRepository.deleteById(id);
        entityManager.flush();

        Optional<Anggota> found = anggotaRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if anggota exists by id")
    void testExistsById() {
        Anggota saved = entityManager.persistAndFlush(testAnggota);

        boolean exists = anggotaRepository.existsById(saved.getId());
        assertThat(exists).isTrue();

        boolean notExists = anggotaRepository.existsById(999L);
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should count all anggota")
    void testCount() {
        entityManager.persist(testAnggota);
        
        Anggota anggota2 = new Anggota();
        anggota2.setNomorAnggota("A002");
        anggota2.setNama("Jane Doe");
        anggota2.setAlamat("Jl. Test");
        anggota2.setEmail("jane@test.com");
        entityManager.persist(anggota2);
        
        entityManager.flush();

        long count = anggotaRepository.count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle multiple anggota with different nomor")
    void testMultipleAnggota() {
        Anggota anggota1 = new Anggota(null, "A001", "John", "Jl. A", "john@test.com");
        Anggota anggota2 = new Anggota(null, "A002", "Jane", "Jl. B", "jane@test.com");
        Anggota anggota3 = new Anggota(null, "A003", "Bob", "Jl. C", "bob@test.com");

        anggotaRepository.save(anggota1);
        anggotaRepository.save(anggota2);
        anggotaRepository.save(anggota3);

        List<Anggota> all = anggotaRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should persist anggota with all fields")
    void testPersistAllFields() {
        Anggota saved = anggotaRepository.save(testAnggota);
        entityManager.flush();
        entityManager.clear();

        Anggota found = anggotaRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getNomorAnggota()).isEqualTo("A001");
        assertThat(found.getNama()).isEqualTo("John Doe");
        assertThat(found.getAlamat()).isEqualTo("Jl. Test No. 123");
        assertThat(found.getEmail()).isEqualTo("john@test.com");
    }
}
package com.perpustakaan.service_anggota.repository.query;

import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnggotaQueryRepository extends MongoRepository<AnggotaReadModel, Long> {
    // MongoRepo otomatis support findBy... sama seperti JPA
    AnggotaReadModel findByNomorAnggota(String nomorAnggota);
}
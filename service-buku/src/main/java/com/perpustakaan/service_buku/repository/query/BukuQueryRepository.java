package com.perpustakaan.service_buku.repository.query;

import com.perpustakaan.service_buku.entity.query.BukuReadModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuQueryRepository extends MongoRepository<BukuReadModel, Long> {
    // MongoRepo otomatis support findBy... sama seperti JPA
    BukuReadModel findByNomorbuku(String nomorbuku);
}
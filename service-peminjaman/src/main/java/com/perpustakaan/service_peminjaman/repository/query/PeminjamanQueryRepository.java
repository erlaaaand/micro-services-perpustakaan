package com.perpustakaan.service_peminjaman.repository.query;

import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PeminjamanQueryRepository extends MongoRepository<PeminjamanReadModel, String> {
    Optional<PeminjamanReadModel> findById(String id);
}
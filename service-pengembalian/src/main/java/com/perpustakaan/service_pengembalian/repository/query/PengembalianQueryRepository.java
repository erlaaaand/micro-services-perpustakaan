package com.perpustakaan.service_pengembalian.repository.query;

import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PengembalianQueryRepository extends MongoRepository<PengembalianReadModel, String> {
}
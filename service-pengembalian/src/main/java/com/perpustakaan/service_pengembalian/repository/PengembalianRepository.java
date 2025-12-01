package com.perpustakaan.service_pengembalian.repository;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PengembalianRepository extends JpaRepository<Pengembalian, Long> {
}
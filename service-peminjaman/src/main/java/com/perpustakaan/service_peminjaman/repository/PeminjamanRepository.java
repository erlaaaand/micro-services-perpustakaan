package com.perpustakaan.service_peminjaman.repository;

import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {
}
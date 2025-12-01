package com.perpustakaan.service_buku.repository;

import com.perpustakaan.service_buku.entity.Buku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuRepository extends JpaRepository<Buku, Long> {
    Buku findByKodeBuku(String kodeBuku);
}
package com.perpustakaan.service_buku.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perpustakaan.service_buku.entity.command.Buku;

@Repository
public interface BukuCommandRepository extends JpaRepository<Buku, Long> {
    Buku findByKodeBuku(String kodeBuku);
}
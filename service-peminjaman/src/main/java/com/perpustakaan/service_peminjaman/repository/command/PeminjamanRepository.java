package com.perpustakaan.service_peminjaman.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perpustakaan.service_peminjaman.entity.command.Peminjaman;

@Repository
public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {
}
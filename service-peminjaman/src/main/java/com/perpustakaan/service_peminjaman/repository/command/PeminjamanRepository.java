package com.perpustakaan.service_peminjaman.repository.command;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perpustakaan.service_peminjaman.entity.command.PeminjamanWriteModel;

@Repository
public interface PeminjamanRepository extends JpaRepository<PeminjamanWriteModel, UUID> {
    boolean existsByAnggotaIdAndBukuIdAndStatus(UUID anggotaId, UUID bukuId, String status);
}
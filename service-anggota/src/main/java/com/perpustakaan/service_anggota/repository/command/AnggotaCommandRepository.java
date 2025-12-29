package com.perpustakaan.service_anggota.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.perpustakaan.service_anggota.entity.command.Anggota;

@Repository
public interface AnggotaCommandRepository extends JpaRepository<Anggota, Long> {
    Anggota findByNomorAnggota(String nomorAnggota);
}

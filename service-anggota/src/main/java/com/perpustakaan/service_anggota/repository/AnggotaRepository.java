package com.perpustakaan.service_anggota.repository;

import com.perpustakaan.service_anggota.entity.Anggota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnggotaRepository extends JpaRepository<Anggota, Long> {
    Anggota findByNomorAnggota(String nomorAnggota);
}
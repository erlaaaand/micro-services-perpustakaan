package com.perpustakaan.service_anggota.repository.command;

import com.perpustakaan.service_anggota.entity.command.AnggotaWriteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnggotaCommandRepository extends JpaRepository<AnggotaWriteModel, UUID> {
    boolean existsByNomorAnggota(String nomorAnggota);
    boolean existsByEmail(String email);
    Optional<AnggotaWriteModel> findByEmail(String email);
    Optional<AnggotaWriteModel> findByNomorAnggota(String nomorAnggota);
}
package com.perpustakaan.service_buku.repository.command;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.perpustakaan.service_buku.entity.command.BukuWriteModel;

@Repository
public interface BukuCommandRepository extends JpaRepository<BukuWriteModel, UUID> {
    
    boolean existsByKodeBuku(String kodeBuku);
    
    boolean existsByJudul(String judul); 

    Optional<BukuWriteModel> findByKodeBuku(String kodeBuku);
    
    Optional<BukuWriteModel> findByJudul(String judul); 
}
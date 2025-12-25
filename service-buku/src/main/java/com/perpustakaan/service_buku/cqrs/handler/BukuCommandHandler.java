package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.repository.BukuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class BukuCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BukuCommandHandler.class);
    
    @Autowired
    private BukuRepository bukuRepository;
    
    @Transactional
    public Buku handle(CreateBukuCommand command) {
        logger.info("Handling CreateBukuCommand for nomor: {}", command.getNomorBuku());

        // Validasi duplikasi
        Buku existing = bukuRepository.findByKodeBuku(command.getNomorBuku());
        if (existing != null) {
            throw new IllegalArgumentException("Nomor buku sudah digunakan: " + command.getNomorBuku());
        }
        
        Buku buku = new Buku();
        buku.setKodeBuku(command.getNomorBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPenulis());
        buku.setTahunTerbit(command.getTahunTerbit());
        
        Buku saved = bukuRepository.save(buku);
        logger.info("Successfully created buku with ID: {}", saved.getId());
        
        return saved;
    }
    
    @Transactional
    public Buku handle(UpdateBukuCommand command) {
        logger.info("Handling UpdateBukuCommand for ID: {}", command.getId());
        
        Optional<Buku> existing = bukuRepository.findById(command.getId());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }
        
        Buku buku = existing.get();

        // Validasi kode buku jika berubah
        if (!buku.getKodeBuku().equals(command.getKodeBuku())) {
            Buku conflict = bukuRepository.findByKodeBuku(command.getKodeBuku());
            if (conflict != null && !conflict.getId().equals(command.getId())) {
                throw new IllegalArgumentException("Kode buku sudah digunakan: " + command.getKodeBuku());
            }
        }

        buku.setKodeBuku(command.getKodeBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPenulis());
        buku.setTahunTerbit(command.getTahunTerbit());
        buku.setAlamat(command.getAlamat());
        buku.setEmail(command.getEmail());
        
        Buku updated = bukuRepository.save(buku);
        logger.info("Successfully updated buku with ID: {}", updated.getId());
        
        return updated;
    }
    
    @Transactional
    public void handle(DeleteBukuCommand command) {
        logger.info("Handling DeleteBukuCommand for ID: {}", command.getId());
        
        if (!bukuRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }

        bukuRepository.deleteById(command.getId());
        logger.info("Successfully deleted buku with ID: {}", command.getId());
    }
}
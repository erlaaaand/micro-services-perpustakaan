package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AnggotaCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaCommandHandler.class);
    
    @Autowired
    private AnggotaRepository anggotaRepository;
    
    @Transactional
    public Anggota handle(CreateAnggotaCommand command) {
        logger.info("Handling CreateAnggotaCommand for nomor: {}", command.getNomorAnggota());
        
        // Validasi duplikasi
        Anggota existing = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
        if (existing != null) {
            throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + command.getNomorAnggota());
        }
        
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        
        Anggota saved = anggotaRepository.save(anggota);
        logger.info("Successfully created anggota with ID: {}", saved.getId());
        
        return saved;
    }
    
    @Transactional
    public Anggota handle(UpdateAnggotaCommand command) {
        logger.info("Handling UpdateAnggotaCommand for ID: {}", command.getId());
        
        Optional<Anggota> existing = anggotaRepository.findById(command.getId());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
        }
        
        Anggota anggota = existing.get();
        
        // Validasi nomor anggota jika berubah
        if (!anggota.getNomorAnggota().equals(command.getNomorAnggota())) {
            Anggota conflict = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
            if (conflict != null && !conflict.getId().equals(command.getId())) {
                throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + command.getNomorAnggota());
            }
        }
        
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        
        Anggota updated = anggotaRepository.save(anggota);
        logger.info("Successfully updated anggota with ID: {}", updated.getId());
        
        return updated;
    }
    
    @Transactional
    public void handle(DeleteAnggotaCommand command) {
        logger.info("Handling DeleteAnggotaCommand for ID: {}", command.getId());
        
        if (!anggotaRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
        }
        
        anggotaRepository.deleteById(command.getId());
        logger.info("Successfully deleted anggota with ID: {}", command.getId());
    }
}
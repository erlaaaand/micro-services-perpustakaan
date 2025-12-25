package com.perpustakaan.service_anggota.service;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnggotaService {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaService.class);

    @Autowired
    private AnggotaRepository anggotaRepository;

    public Anggota saveAnggota(AnggotaRequest request) {
        logger.debug("Saving new anggota with nomor: {}", request.getNomorAnggota());
        
        // Check if nomor anggota already exists
        Anggota existing = anggotaRepository.findByNomorAnggota(request.getNomorAnggota());
        if (existing != null) {
            logger.warn("Anggota with nomor {} already exists", request.getNomorAnggota());
            throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + request.getNomorAnggota());
        }
        
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(request.getNomorAnggota());
        anggota.setNama(request.getNama());
        anggota.setAlamat(request.getAlamat());
        anggota.setEmail(request.getEmail());
        
        Anggota saved = anggotaRepository.save(anggota);
        logger.info("Successfully saved anggota with ID: {} and nomor: {}", 
                    saved.getId(), saved.getNomorAnggota());
        return saved;
    }

    @Transactional(readOnly = true)
    public Anggota getAnggotaById(Long id) {
        logger.debug("Fetching anggota by ID: {}", id);
        return anggotaRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Anggota> getAllAnggota() {
        logger.debug("Fetching all anggota");
        List<Anggota> anggotaList = anggotaRepository.findAll();
        logger.debug("Found {} anggota records", anggotaList.size());
        return anggotaList;
    }

    public Anggota updateAnggota(Long id, AnggotaRequest request) {
        logger.debug("Updating anggota with ID: {}", id);
        
        Optional<Anggota> existing = anggotaRepository.findById(id);
        if (existing.isPresent()) {
            Anggota anggota = existing.get();
            
            // Check if new nomor anggota conflicts with another record
            if (!anggota.getNomorAnggota().equals(request.getNomorAnggota())) {
                Anggota conflict = anggotaRepository.findByNomorAnggota(request.getNomorAnggota());
                if (conflict != null && !conflict.getId().equals(id)) {
                    logger.warn("Cannot update - nomor anggota {} already exists", request.getNomorAnggota());
                    throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + request.getNomorAnggota());
                }
            }
            
            anggota.setNomorAnggota(request.getNomorAnggota());
            anggota.setNama(request.getNama());
            anggota.setAlamat(request.getAlamat());
            anggota.setEmail(request.getEmail());
            
            Anggota updated = anggotaRepository.save(anggota);
            logger.info("Successfully updated anggota with ID: {}", id);
            return updated;
        }
        
        logger.warn("Cannot update - anggota with ID {} not found", id);
        return null;
    }

    public boolean deleteAnggota(Long id) {
        logger.debug("Attempting to delete anggota with ID: {}", id);
        
        if (anggotaRepository.existsById(id)) {
            anggotaRepository.deleteById(id);
            logger.info("Successfully deleted anggota with ID: {}", id);
            return true;
        }
        
        logger.warn("Cannot delete - anggota with ID {} not found", id);
        return false;
    }
}
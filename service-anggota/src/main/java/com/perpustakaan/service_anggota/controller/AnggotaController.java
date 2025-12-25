package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.service.AnggotaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anggota")
public class AnggotaController {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaController.class);

    @Autowired
    private AnggotaService anggotaService;

    @PostMapping
    public ResponseEntity<Anggota> saveAnggota(@Valid @RequestBody AnggotaRequest request) {
        logger.info("Creating new anggota: {}", request.getNomorAnggota());
        Anggota saved = anggotaService.saveAnggota(request);
        logger.info("Successfully created anggota with ID: {}", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anggota> getAnggotaById(@PathVariable("id") Long id) {
        logger.info("Fetching anggota with ID: {}", id);
        Anggota anggota = anggotaService.getAnggotaById(id);
        if (anggota != null) {
            logger.info("Found anggota: {}", anggota.getNomorAnggota());
            return ResponseEntity.ok(anggota);
        }
        logger.warn("Anggota with ID {} not found", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Anggota>> getAllAnggota() {
        logger.info("Fetching all anggota");
        List<Anggota> anggotaList = anggotaService.getAllAnggota();
        logger.info("Found {} anggota records", anggotaList.size());
        return ResponseEntity.ok(anggotaList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Anggota> updateAnggota(@PathVariable("id") Long id, 
                                                  @Valid @RequestBody AnggotaRequest request) {
        logger.info("Updating anggota with ID: {}", id);
        Anggota updated = anggotaService.updateAnggota(id, request);
        if (updated != null) {
            logger.info("Successfully updated anggota: {}", updated.getNomorAnggota());
            return ResponseEntity.ok(updated);
        }
        logger.warn("Cannot update - anggota with ID {} not found", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnggota(@PathVariable("id") Long id) {
        logger.info("Deleting anggota with ID: {}", id);
        boolean deleted = anggotaService.deleteAnggota(id);
        if (deleted) {
            logger.info("Successfully deleted anggota with ID: {}", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Cannot delete - anggota with ID {} not found", id);
        return ResponseEntity.notFound().build();
    }
}
package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.service.AnggotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anggota")
public class AnggotaController {

    @Autowired
    private AnggotaService anggotaService;

    @PostMapping
    public ResponseEntity<Anggota> saveAnggota(@RequestBody AnggotaRequest request) {
        Anggota saved = anggotaService.saveAnggota(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anggota> getAnggotaById(@PathVariable("id") Long id) {
        Anggota anggota = anggotaService.getAnggotaById(id);
        if (anggota != null) {
            return ResponseEntity.ok(anggota);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Anggota>> getAllAnggota() {
        return ResponseEntity.ok(anggotaService.getAllAnggota());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Anggota> updateAnggota(@PathVariable("id") Long id, @RequestBody AnggotaRequest request) {
        Anggota updated = anggotaService.updateAnggota(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnggota(@PathVariable("id") Long id) {
        boolean deleted = anggotaService.deleteAnggota(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
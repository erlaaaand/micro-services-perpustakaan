package com.perpustakaan.service_peminjaman.controller;

import com.perpustakaan.service_peminjaman.dto.PeminjamanRequest;
import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.service.PeminjamanService;
import com.perpustakaan.service_peminjaman.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {
    
    @Autowired
    private PeminjamanService peminjamanService;

    @PostMapping
    public ResponseEntity<Peminjaman> savePeminjaman(@RequestBody PeminjamanRequest request) {
        Peminjaman saved = peminjamanService.savePeminjaman(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseTemplateVO> getPeminjaman(@PathVariable("id") Long id) {
        ResponseTemplateVO response = peminjamanService.getPeminjamanWithDetails(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Peminjaman>> getAllPeminjaman() {
        return ResponseEntity.ok(peminjamanService.getAllPeminjaman());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Peminjaman> updatePeminjaman(@PathVariable("id") Long id, @RequestBody PeminjamanRequest request) {
        Peminjaman updated = peminjamanService.updatePeminjaman(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeminjaman(@PathVariable("id") Long id) {
        boolean deleted = peminjamanService.deletePeminjaman(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Peminjaman> updateStatus(@PathVariable("id") Long id, @RequestParam String status) {
        Peminjaman updated = peminjamanService.updateStatus(id, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}
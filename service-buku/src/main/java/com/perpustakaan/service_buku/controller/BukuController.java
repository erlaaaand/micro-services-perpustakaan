package com.perpustakaan.service_buku.controller;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.cqrs.handler.*;
import com.perpustakaan.service_buku.cqrs.query.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.perpustakaan.service_buku.dto.BukuRequest;
import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.service.BukuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/buku")
public class BukuController {

    @Autowired
    private BukuService bukuService;

    @PostMapping
    public ResponseEntity<Buku> saveBuku(@RequestBody BukuRequest request) {
        Buku saved = bukuService.saveBuku(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Buku> getBukuById(@PathVariable("id") Long id) {
        Buku buku = bukuService.getBukuById(id);
        if (buku != null) {
            return ResponseEntity.ok(buku);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Buku>> getAllBuku() {
        return ResponseEntity.ok(bukuService.getAllBuku());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Buku> updateBuku(@PathVariable("id") Long id, @RequestBody BukuRequest request) {
        Buku updated = bukuService.updateBuku(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuku(@PathVariable("id") Long id) {
        boolean deleted = bukuService.deleteBuku(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
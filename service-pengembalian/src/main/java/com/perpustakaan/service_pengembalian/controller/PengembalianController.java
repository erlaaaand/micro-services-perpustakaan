package com.perpustakaan.service_pengembalian.controller;

import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;
import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.service.PengembalianService;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pengembalian")
public class PengembalianController {

    @Autowired
    private PengembalianService pengembalianService;

    @PostMapping
    public ResponseEntity<Pengembalian> savePengembalian(@RequestBody PengembalianRequest request) {
        Pengembalian saved = pengembalianService.savePengembalian(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseTemplateVO> getPengembalian(@PathVariable("id") Long id) {
        ResponseTemplateVO response = pengembalianService.getPengembalianWithDetails(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Pengembalian>> getAllPengembalian() {
        return ResponseEntity.ok(pengembalianService.getAllPengembalian());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pengembalian> updatePengembalian(@PathVariable("id") Long id, @RequestBody PengembalianRequest request) {
        Pengembalian updated = pengembalianService.updatePengembalian(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePengembalian(@PathVariable("id") Long id) {
        boolean deleted = pengembalianService.deletePengembalian(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
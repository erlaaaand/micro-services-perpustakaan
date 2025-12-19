package com.perpustakaan.service_pengembalian.controller;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.service.PengembalianService;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;

@RestController
@RequestMapping("/api/pengembalian")
public class PengembalianController {

    @Autowired
    private PengembalianService pengembalianService;

    @PostMapping
    public Pengembalian savePengembalian(@RequestBody PengembalianRequest request) {
        return pengembalianService.savePengembalian(request);
    }

    @GetMapping("/{id}")
    public ResponseTemplateVO getPengembalian(@PathVariable("id") Long id) {
        return pengembalianService.getPengembalian(id);
    }

    @PutMapping("/{id}")
    public Pengembalian updatePengembalian(@PathVariable("id") Long id, @RequestBody PengembalianRequest request) {
        return pengembalianService.updatePengembalian(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePengembalian(@PathVariable("id") Long id) {
        pengembalianService.deletePengembalian(id);
    }
}
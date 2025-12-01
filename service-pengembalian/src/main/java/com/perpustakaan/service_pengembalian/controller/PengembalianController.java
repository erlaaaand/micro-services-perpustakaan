package com.perpustakaan.service_pengembalian.controller;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.service.PengembalianService;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pengembalian")
public class PengembalianController {

    @Autowired
    private PengembalianService pengembalianService;

    @PostMapping
    public Pengembalian savePengembalian(@RequestBody Pengembalian pengembalian) {
        return pengembalianService.savePengembalian(pengembalian);
    }

    @GetMapping("/{id}")
    public ResponseTemplateVO getPengembalian(@PathVariable("id") Long id) {
        return pengembalianService.getPengembalian(id);
    }
}
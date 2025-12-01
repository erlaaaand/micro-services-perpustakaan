package com.perpustakaan.service_peminjaman.controller;

import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.service.PeminjamanService;
import com.perpustakaan.service_peminjaman.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {
    @Autowired
    private PeminjamanService peminjamanService;

    @PostMapping
    public Peminjaman savePeminjaman(@RequestBody Peminjaman peminjaman) {
        return peminjamanService.savePeminjaman(peminjaman);
    }

    @GetMapping("/{id}")
    public ResponseTemplateVO getPeminjaman(@PathVariable("id") Long id) {
        return peminjamanService.getPeminjaman(id);
    }
}   
package com.perpustakaan.service_buku.controller;

import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.service.BukuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/buku")
public class BukuController {

    @Autowired
    private BukuService bukuService;

    @PostMapping
    public Buku saveBuku(@RequestBody Buku buku) {
        return bukuService.saveBuku(buku);
    }

    @GetMapping("/{id}")
    public Buku getBukuById(@PathVariable("id") Long id) {
        return bukuService.getBukuById(id);
    }

    @GetMapping
    public List<Buku> getAllBuku() {
        return bukuService.getAllBuku();
    }
}
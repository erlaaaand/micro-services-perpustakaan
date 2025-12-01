package com.perpustakaan.service_buku.controller;

import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.service.BukuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.perpustakaan.service_buku.dto.BukuRequest;

@RestController
@RequestMapping("/api/buku")
public class BukuController {

    @Autowired
    private BukuService bukuService;

    @PostMapping
    public Buku saveBuku(@RequestBody BukuRequest request) {
        return bukuService.saveBuku(request);
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
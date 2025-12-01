package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.service.AnggotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anggota")
public class AnggotaController {

    @Autowired
    private AnggotaService anggotaService;

    // POST: Menambah Anggota Baru
    @PostMapping
    public Anggota saveAnggota(@RequestBody Anggota anggota) {
        return anggotaService.saveAnggota(anggota);
    }

    // GET: Mengambil 1 Anggota berdasarkan ID
    @GetMapping("/{id}")
    public Anggota getAnggotaById(@PathVariable("id") Long id) {
        return anggotaService.getAnggotaById(id);
    }

    // GET: Mengambil Semua Anggota
    @GetMapping
    public List<Anggota> getAllAnggota() {
        return anggotaService.getAllAnggota();
    }
}
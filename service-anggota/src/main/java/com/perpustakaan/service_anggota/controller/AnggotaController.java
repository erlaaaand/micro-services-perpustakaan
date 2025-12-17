package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
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

    @PostMapping
    public Anggota saveAnggota(@RequestBody AnggotaRequest request) {
        return anggotaService.saveAnggota(request);
    }

    @GetMapping("/{id}")
    public Anggota getAnggotaById(@PathVariable("id") Long id) {
        return anggotaService.getAnggotaById(id);
    }

    @GetMapping
    public List<Anggota> getAllAnggota() {
        return anggotaService.getAllAnggota();
    }

    @PutMapping("/{id}")
    public Anggota updateAnggota(@PathVariable("id") Long id, @RequestBody AnggotaRequest request) {
        return anggotaService.updateAnggota(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAnggota(@PathVariable("id") Long id) {
        anggotaService.deleteAnggota(id);
    }
}
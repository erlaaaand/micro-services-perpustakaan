package com.perpustakaan.service_buku.service;

import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.repository.BukuRepository;
import com.perpustakaan.service_buku.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.perpustakaan.service_buku.dto.BukuRequest;

@Service
public class BukuService {
    @Autowired
    private BukuRepository bukuRepository;

    public Buku saveBuku(BukuRequest request) {
        Buku buku = new Buku();
        buku.setKodeBuku(request.getKodeBuku());
        buku.setJudul(request.getJudul());
        buku.setPengarang(request.getPengarang());
        buku.setPenerbit(request.getPenerbit());
        buku.setTahunTerbit(request.getTahunTerbit());
        return bukuRepository.save(buku);
    }

    public Buku getBukuById(Long id) {
        return bukuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buku dengan ID " + id + " tidak ditemukan!"));
    }

    public List<Buku> getAllBuku() {
        return bukuRepository.findAll();
    }

    public Buku updateBuku(Long id, BukuRequest request) {
        Buku buku = bukuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gagal Update: Buku dengan ID " + id + " tidak ditemukan!"));

        buku.setKodeBuku(request.getKodeBuku());
        buku.setJudul(request.getJudul());
        buku.setPengarang(request.getPengarang());
        buku.setPenerbit(request.getPenerbit());
        buku.setTahunTerbit(request.getTahunTerbit());
        
        return bukuRepository.save(buku);
    }

    public void deleteBuku(Long id) {
        if (!bukuRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gagal Hapus: Buku dengan ID " + id + " tidak ditemukan!");
        }
        bukuRepository.deleteById(id);
    }
}
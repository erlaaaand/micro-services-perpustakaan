package com.perpustakaan.service_buku.service;

import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.repository.BukuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BukuService {
    @Autowired
    private BukuRepository bukuRepository;

    public Buku saveBuku(Buku buku) {
        return bukuRepository.save(buku);
    }

    public Buku getBukuById(Long id) {
        return bukuRepository.findById(id).orElse(null);
    }

    public List<Buku> getAllBuku() {
        return bukuRepository.findAll();
    }
}
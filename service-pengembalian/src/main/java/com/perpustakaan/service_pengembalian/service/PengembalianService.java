package com.perpustakaan.service_pengembalian.service;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.repository.PengembalianRepository;
import com.perpustakaan.service_pengembalian.vo.Peminjaman;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PengembalianService {

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Pengembalian savePengembalian(Pengembalian pengembalian) {
        return pengembalianRepository.save(pengembalian);
    }

    public ResponseTemplateVO getPengembalian(Long id) {
        ResponseTemplateVO vo = new ResponseTemplateVO();
        Pengembalian pengembalian = pengembalianRepository.findById(id).get();

        // Ambil data Peminjaman dari Service Peminjaman (Port 8083)
        Peminjaman peminjaman = restTemplate.getForObject(
            "http://localhost:8083/api/peminjaman/" + pengembalian.getPeminjamanId(),
            Peminjaman.class
        );

        vo.setPengembalian(pengembalian);
        vo.setPeminjaman(peminjaman);
        return vo;
    }
}
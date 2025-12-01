package com.perpustakaan.service_peminjaman.service;

import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.repository.PeminjamanRepository;
import com.perpustakaan.service_peminjaman.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.perpustakaan.service_peminjaman.dto.PeminjamanRequest;

@Service
public class PeminjamanService {
    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Peminjaman savePeminjaman(PeminjamanRequest request) {
        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setAnggotaId(request.getAnggotaId());
        peminjaman.setBukuId(request.getBukuId());
        peminjaman.setTanggalPinjam(request.getTanggalPinjam());
        peminjaman.setTanggalKembali(request.getTanggalKembali());
        peminjaman.setStatus(request.getStatus());
        return peminjamanRepository.save(peminjaman);
    }

    public ResponseTemplateVO getPeminjaman(Long peminjamanId) {
        ResponseTemplateVO vo = new ResponseTemplateVO();
        Peminjaman peminjaman = peminjamanRepository.findById(peminjamanId).get();
        
        Anggota anggota = restTemplate.getForObject("http://localhost:8081/api/anggota/" + peminjaman.getAnggotaId(), Anggota.class);

        Buku buku = restTemplate.getForObject("http://localhost:8082/api/buku/" + peminjaman.getBukuId(), Buku.class);

        vo.setPeminjaman(peminjaman);
        vo.setAnggota(anggota);
        vo.setBuku(buku);
        return vo;
    }
}
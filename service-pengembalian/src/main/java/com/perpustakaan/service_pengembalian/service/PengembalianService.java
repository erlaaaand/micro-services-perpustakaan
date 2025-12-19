package com.perpustakaan.service_pengembalian.service;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.repository.PengembalianRepository;
import com.perpustakaan.service_pengembalian.vo.Peminjaman;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import com.perpustakaan.service_pengembalian.exception.ResourceNotFoundException; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;

@Service
public class PengembalianService {

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Pengembalian savePengembalian(PengembalianRequest request) {
        Pengembalian pengembalian = new Pengembalian();
        pengembalian.setPeminjamanId(request.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(request.getTanggalDikembalikan());
        pengembalian.setTerlambat(request.getTerlambat());
        pengembalian.setDenda(request.getDenda());
        return pengembalianRepository.save(pengembalian);
    }

    public ResponseTemplateVO getPengembalian(Long id) {
        ResponseTemplateVO vo = new ResponseTemplateVO();
        
        Pengembalian pengembalian = pengembalianRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Data Pengembalian dengan ID " + id + " tidak ditemukan!"));

        Peminjaman peminjaman = restTemplate.getForObject(
            "http://localhost:8083/api/peminjaman/" + pengembalian.getPeminjamanId(),
            Peminjaman.class
        );

        vo.setPengembalian(pengembalian);
        vo.setPeminjaman(peminjaman);
        return vo;
    }

    public Pengembalian updatePengembalian(Long id, PengembalianRequest request) {
        Pengembalian pengembalian = pengembalianRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gagal Update: Data Pengembalian ID " + id + " tidak ditemukan!"));

        pengembalian.setPeminjamanId(request.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(request.getTanggalDikembalikan());
        pengembalian.setTerlambat(request.getTerlambat());
        pengembalian.setDenda(request.getDenda());
        
        return pengembalianRepository.save(pengembalian);
    }

    public void deletePengembalian(Long id) {
        if (!pengembalianRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gagal Hapus: Data Pengembalian ID " + id + " tidak ditemukan!");
        }
        pengembalianRepository.deleteById(id);
    }
}
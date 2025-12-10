package com.perpustakaan.service_peminjaman.service;

import com.perpustakaan.service_peminjaman.dto.PeminjamanRequest;
import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.repository.PeminjamanRepository;
import com.perpustakaan.service_peminjaman.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

@Service
public class PeminjamanService {
    
    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private DiscoveryClient discoveryClient;

    public Peminjaman savePeminjaman(PeminjamanRequest request) {
        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setAnggotaId(request.getAnggotaId());
        peminjaman.setBukuId(request.getBukuId());
        peminjaman.setTanggalPinjam(request.getTanggalPinjam());
        peminjaman.setTanggalKembali(request.getTanggalKembali());
        peminjaman.setStatus(request.getStatus());
        return peminjamanRepository.save(peminjaman);
    }

    public ResponseTemplateVO getPeminjamanWithDetails(Long peminjamanId) {
        Optional<Peminjaman> peminjamanOpt = peminjamanRepository.findById(peminjamanId);
        if (!peminjamanOpt.isPresent()) {
            return null;
        }

        Peminjaman peminjaman = peminjamanOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();
        
        try {
            // Gunakan Service Discovery untuk mendapatkan URL service
            String anggotaUrl = getServiceUrl("service-anggota");
            String bukuUrl = getServiceUrl("service-buku");
            
            Anggota anggota = restTemplate.getForObject(
                anggotaUrl + "/api/anggota/" + peminjaman.getAnggotaId(), 
                Anggota.class
            );

            Buku buku = restTemplate.getForObject(
                bukuUrl + "/api/buku/" + peminjaman.getBukuId(), 
                Buku.class
            );

            vo.setPeminjaman(peminjaman);
            vo.setAnggota(anggota);
            vo.setBuku(buku);
        } catch (Exception e) {
            // Jika service tidak tersedia, tetap return data peminjaman
            vo.setPeminjaman(peminjaman);
        }
        
        return vo;
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanRepository.findAll();
    }

    public Peminjaman updatePeminjaman(Long id, PeminjamanRequest request) {
        Optional<Peminjaman> existing = peminjamanRepository.findById(id);
        if (existing.isPresent()) {
            Peminjaman peminjaman = existing.get();
            peminjaman.setAnggotaId(request.getAnggotaId());
            peminjaman.setBukuId(request.getBukuId());
            peminjaman.setTanggalPinjam(request.getTanggalPinjam());
            peminjaman.setTanggalKembali(request.getTanggalKembali());
            peminjaman.setStatus(request.getStatus());
            return peminjamanRepository.save(peminjaman);
        }
        return null;
    }

    public Peminjaman updateStatus(Long id, String status) {
        Optional<Peminjaman> existing = peminjamanRepository.findById(id);
        if (existing.isPresent()) {
            Peminjaman peminjaman = existing.get();
            peminjaman.setStatus(status);
            return peminjamanRepository.save(peminjaman);
        }
        return null;
    }

    public boolean deletePeminjaman(Long id) {
        if (peminjamanRepository.existsById(id)) {
            peminjamanRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        throw new RuntimeException("Service " + serviceName + " not found");
    }
}
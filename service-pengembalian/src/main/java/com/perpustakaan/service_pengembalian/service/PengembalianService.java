package com.perpustakaan.service_pengembalian.service;

import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;
import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.repository.PengembalianRepository;
import com.perpustakaan.service_pengembalian.vo.Peminjaman;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

@Service
public class PengembalianService {

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private DiscoveryClient discoveryClient;

    public Pengembalian savePengembalian(PengembalianRequest request) {
        Pengembalian pengembalian = new Pengembalian();
        pengembalian.setPeminjamanId(request.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(request.getTanggalDikembalikan());
        pengembalian.setTerlambat(request.getTerlambat());
        pengembalian.setDenda(request.getDenda());
        return pengembalianRepository.save(pengembalian);
    }

    public ResponseTemplateVO getPengembalianWithDetails(Long id) {
        Optional<Pengembalian> pengembalianOpt = pengembalianRepository.findById(id);
        if (!pengembalianOpt.isPresent()) {
            return null;
        }

        Pengembalian pengembalian = pengembalianOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();

        try {
            // Gunakan Service Discovery untuk mendapatkan URL service
            String peminjamanUrl = getServiceUrl("service-peminjaman");
            
            // Ambil data Peminjaman dari Service Peminjaman
            Peminjaman peminjaman = restTemplate.getForObject(
                peminjamanUrl + "/api/peminjaman/" + pengembalian.getPeminjamanId(),
                Peminjaman.class
            );

            vo.setPengembalian(pengembalian);
            vo.setPeminjaman(peminjaman);
        } catch (Exception e) {
            // Jika service tidak tersedia, tetap return data pengembalian
            vo.setPengembalian(pengembalian);
        }

        return vo;
    }

    public List<Pengembalian> getAllPengembalian() {
        return pengembalianRepository.findAll();
    }

    public Pengembalian updatePengembalian(Long id, PengembalianRequest request) {
        Optional<Pengembalian> existing = pengembalianRepository.findById(id);
        if (existing.isPresent()) {
            Pengembalian pengembalian = existing.get();
            pengembalian.setPeminjamanId(request.getPeminjamanId());
            pengembalian.setTanggalDikembalikan(request.getTanggalDikembalikan());
            pengembalian.setTerlambat(request.getTerlambat());
            pengembalian.setDenda(request.getDenda());
            return pengembalianRepository.save(pengembalian);
        }
        return null;
    }

    public boolean deletePengembalian(Long id) {
        if (pengembalianRepository.existsById(id)) {
            pengembalianRepository.deleteById(id);
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
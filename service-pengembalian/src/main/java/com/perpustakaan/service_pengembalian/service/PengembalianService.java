package com.perpustakaan.service_pengembalian.service;

import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.repository.PengembalianRepository;
import com.perpustakaan.service_pengembalian.vo.Peminjaman;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import java.util.List;

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

    public ResponseTemplateVO getPengembalian(Long id) {
        ResponseTemplateVO vo = new ResponseTemplateVO();
        Pengembalian pengembalian = pengembalianRepository.findById(id).get();

        // Gunakan Service Discovery untuk mendapatkan URL service
        String peminjamanUrl = getServiceUrl("service-peminjaman");
        
        // Ambil data Peminjaman dari Service Peminjaman
        Peminjaman peminjaman = restTemplate.getForObject(
            peminjamanUrl + "/api/peminjaman/" + pengembalian.getPeminjamanId(),
            Peminjaman.class
        );

        vo.setPengembalian(pengembalian);
        vo.setPeminjaman(peminjaman);
        return vo;
    }
    
    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        throw new RuntimeException("Service " + serviceName + " not found");
    }
}
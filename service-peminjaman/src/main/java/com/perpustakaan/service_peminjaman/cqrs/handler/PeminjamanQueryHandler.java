package com.perpustakaan.service_peminjaman.cqrs.handler;

import com.perpustakaan.service_peminjaman.cqrs.query.*;
import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.repository.PeminjamanRepository;
import com.perpustakaan.service_peminjaman.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class PeminjamanQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanQueryHandler.class);

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Transactional(readOnly = true)
    public ResponseTemplateVO handle(GetPeminjamanById query) {
        logger.debug("Handling GetPeminjamanById for ID: {}", query.getId());
        
        Optional<Peminjaman> peminjamanOpt = peminjamanRepository.findById(query.getId());
        if (!peminjamanOpt.isPresent()) {
            return null;
        }

        Peminjaman peminjaman = peminjamanOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();
        vo.setPeminjaman(peminjaman);

        try {
            // Ambil URL service dari Eureka
            String anggotaUrl = getServiceUrl("service-anggota");
            String bukuUrl = getServiceUrl("service-buku");

            // Fetch Anggota
            Anggota anggota = restTemplate.getForObject(
                anggotaUrl + "/api/anggota/" + peminjaman.getAnggotaId(), 
                Anggota.class
            );
            vo.setAnggota(anggota);

            // Fetch Buku
            Buku buku = restTemplate.getForObject(
                bukuUrl + "/api/buku/" + peminjaman.getBukuId(), 
                Buku.class
            );
            vo.setBuku(buku);

        } catch (Exception e) {
            logger.warn("Gagal mengambil detail microservice (Anggota/Buku): {}", e.getMessage());
            // Fallback: biarkan object Anggota/Buku null, tetap kembalikan data Peminjaman
        }

        return vo;
    }

    @Transactional(readOnly = true)
    public Page<Peminjaman> handle(GetAllPeminjaman query) {
        logger.debug("Handling GetAllPeminjaman - page: {}, size: {}", query.getPage(), query.getSize());
        
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize());
        return peminjamanRepository.findAll(pageRequest);
    }

    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        throw new RuntimeException("Service " + serviceName + " not found di Eureka");
    }
}
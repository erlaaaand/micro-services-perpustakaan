package com.perpustakaan.service_peminjaman.cqrs.handler;

import com.perpustakaan.service_peminjaman.cqrs.query.*;
import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import com.perpustakaan.service_peminjaman.repository.query.PeminjamanQueryRepository;
import com.perpustakaan.service_peminjaman.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class PeminjamanQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanQueryHandler.class);

    @Autowired
    private PeminjamanQueryRepository peminjamanRepository; // Mongo Read Repo

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseTemplateVO handle(GetPeminjamanById query) {
        Optional<PeminjamanReadModel> peminjamanOpt = peminjamanRepository.findById(query.getId());
        if (peminjamanOpt.isEmpty()) {
            return null;
        }

        PeminjamanReadModel peminjaman = peminjamanOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();
        vo.setPeminjaman(peminjaman);

        // Fetch detail ke service lain (tetap pakai RestTemplate)
        try {
            String anggotaUrl = getServiceUrl("service-anggota");
            String bukuUrl = getServiceUrl("service-buku");

            if (anggotaUrl != null) {
                Anggota anggota = restTemplate.getForObject(
                    anggotaUrl + "/api/anggota/" + peminjaman.getAnggotaId(), 
                    Anggota.class
                );
                vo.setAnggota(anggota);
            }

            if (bukuUrl != null) {
                Buku buku = restTemplate.getForObject(
                    bukuUrl + "/api/buku/" + peminjaman.getBukuId(), 
                    Buku.class
                );
                vo.setBuku(buku);
            }

        } catch (Exception e) {
            logger.warn("Gagal mengambil detail microservice: {}", e.getMessage());
        }

        return vo;
    }

    public Page<PeminjamanReadModel> handle(GetAllPeminjaman query) {
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize());
        return peminjamanRepository.findAll(pageRequest);
    }

    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        return null;
    }
}
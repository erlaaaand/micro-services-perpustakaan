package com.perpustakaan.service_pengembalian.cqrs.handler;

import com.perpustakaan.service_pengembalian.cqrs.query.*;
import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import com.perpustakaan.service_pengembalian.repository.query.PengembalianQueryRepository;
import com.perpustakaan.service_pengembalian.vo.Peminjaman;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
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
public class PengembalianQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianQueryHandler.class);

    @Autowired
    private PengembalianQueryRepository pengembalianRepository; // Mongo Read Repo

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseTemplateVO handle(GetPengembalianByIdQuery query) {
        Optional<PengembalianReadModel> pengembalianOpt = pengembalianRepository.findById(query.getId());
        if (pengembalianOpt.isEmpty()) {
            return null;
        }

        PengembalianReadModel pengembalian = pengembalianOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();
        vo.setPengembalian(pengembalian);

        try {
            String peminjamanUrl = getServiceUrl("service-peminjaman");
            if (peminjamanUrl != null) {
                Peminjaman peminjaman = restTemplate.getForObject(
                    peminjamanUrl + "/api/peminjaman/" + pengembalian.getPeminjamanId(),
                    Peminjaman.class
                );
                vo.setPeminjaman(peminjaman);
            }
        } catch (Exception e) {
            logger.warn("Gagal mengambil data Peminjaman: {}", e.getMessage());
        }

        return vo;
    }

    public Page<PengembalianReadModel> handle(GetAllPengembalianQuery query) {
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize());
        return pengembalianRepository.findAll(pageRequest);
    }

    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        return null;
    }
}
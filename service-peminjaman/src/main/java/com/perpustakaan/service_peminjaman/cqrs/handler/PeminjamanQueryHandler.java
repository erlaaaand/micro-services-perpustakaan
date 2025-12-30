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
    private PeminjamanQueryRepository peminjamanRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseTemplateVO handle(GetPeminjamanById query) {
        // LOG 1: Mencatat request masuk
        logger.info("Handling query GetPeminjamanById for ID: {}", query.getId());

        Optional<PeminjamanReadModel> peminjamanOpt = peminjamanRepository.findById(query.getId());
        
        if (peminjamanOpt.isEmpty()) {
            // LOG 2: Mencatat jika data tidak ditemukan di Mongo
            logger.warn("Peminjaman dengan ID {} tidak ditemukan di database MongoDB", query.getId());
            return null;
        }

        PeminjamanReadModel peminjaman = peminjamanOpt.get();
        ResponseTemplateVO vo = new ResponseTemplateVO();
        vo.setPeminjaman(peminjaman);

        try {
            logger.debug("Memulai proses fetch detail ke service eksternal untuk Anggota ID: {} dan Buku ID: {}", 
                peminjaman.getAnggotaId(), peminjaman.getBukuId());

            String anggotaUrl = getServiceUrl("service-anggota");
            String bukuUrl = getServiceUrl("service-buku");

            if (anggotaUrl != null) {
                logger.debug("Fetching Anggota dari: {}/api/anggota/{}", anggotaUrl, peminjaman.getAnggotaId());
                Anggota anggota = restTemplate.getForObject(
                    anggotaUrl + "/api/anggota/" + peminjaman.getAnggotaId(), 
                    Anggota.class
                );
                vo.setAnggota(anggota);
            } else {
                logger.error("Service URL untuk service-anggota tidak ditemukan di Eureka!");
            }

            if (bukuUrl != null) {
                logger.debug("Fetching Buku dari: {}/api/buku/{}", bukuUrl, peminjaman.getBukuId());
                Buku buku = restTemplate.getForObject(
                    bukuUrl + "/api/buku/" + peminjaman.getBukuId(), 
                    Buku.class
                );
                vo.setBuku(buku);
            } else {
                logger.error("Service URL untuk service-buku tidak ditemukan di Eureka!");
            }

        } catch (Exception e) {
            // LOG 3: Mencatat error detail (stacktrace) jika terjadi kegagalan sistem
            logger.error("Terjadi kesalahan sistem saat menghubungi microservice lain: {}", e.getMessage(), e);
        }

        logger.info("Berhasil mengembalikan data ResponseTemplateVO untuk Peminjaman ID: {}", query.getId());
        return vo;
    }

    public Page<PeminjamanReadModel> handle(GetAllPeminjaman query) {
        logger.info("Handling GetAllPeminjaman - Page: {}, Size: {}", query.getPage(), query.getSize());
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize());
        Page<PeminjamanReadModel> result = peminjamanRepository.findAll(pageRequest);
        logger.debug("Berhasil mengambil {} data dari MongoDB", result.getNumberOfElements());
        return result;
    }

    private String getServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            String uri = instances.get(0).getUri().toString();
            logger.debug("Eureka Discovery: Service {} ditemukan di URI {}", serviceName, uri);
            return uri;
        }
        logger.error("Eureka Discovery: Service {} TIDAK ditemukan!", serviceName);
        return null;
    }
}
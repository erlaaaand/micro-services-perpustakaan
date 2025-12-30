package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.query.*;
import com.perpustakaan.service_buku.entity.query.BukuReadModel;
import com.perpustakaan.service_buku.repository.query.BukuQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class BukuQueryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BukuQueryHandler.class);
    
    @Autowired
    private BukuQueryRepository bukuRepository;
    
    public BukuReadModel handle(GetBukuByIdQuery query) {
        logger.debug("Handling GetBukuByIdQuery for ID: {}", query.getId());
        return bukuRepository.findById(query.getId()).orElse(null);
    }
    
    public Page<BukuReadModel> handle(GetAllBukuQuery query) {
        logger.debug("Handling GetAllBukuQuery - page: {}, size: {}", query.getPage(), query.getSize());
        PageRequest pageRequest = PageRequest.of(
            query.getPage(), 
            query.getSize(), 
            Sort.by(query.getSortBy()).ascending()
        );
        return bukuRepository.findAll(pageRequest);
    }
    
    // Fix: Ganti dari 'findByNomorbuku' menjadi 'findByKodeBuku'
    public BukuReadModel handle(GetBukuByNomorQuery query) {
        logger.debug("Handling GetBukuByNomorQuery for kode: {}", query.getNomorBuku());
        return bukuRepository.findByKodeBuku(query.getNomorBuku());
    }
}
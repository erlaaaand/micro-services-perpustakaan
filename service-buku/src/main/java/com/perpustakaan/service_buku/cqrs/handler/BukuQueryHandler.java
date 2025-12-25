package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.query.*;
import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.repository.BukuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BukuQueryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BukuQueryHandler.class);
    
    @Autowired
    private BukuRepository bukuRepository;
    
    @Transactional(readOnly = true)
    public Buku handle(GetBukuByIdQuery query) {
        logger.debug("Handling GetBukuByIdQuery for ID: {}", query.getId());
        return bukuRepository.findById(query.getId()).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Page<Buku> handle(GetAllBukuQuery query) {
        logger.debug("Handling GetAllBukuQuery - page: {}, size: {}", query.getPage(), query.getSize());
        
        PageRequest pageRequest = PageRequest.of(
            query.getPage(), 
            query.getSize(), 
            Sort.by(query.getSortBy()).ascending()
        );
        
        return bukuRepository.findAll(pageRequest);
    }
    
    @Transactional(readOnly = true)
    public Buku handle(GetBukuByNomorQuery query) {
        logger.debug("Handling GetBukuByNomorQuery for nomor: {}", query.getNomorBuku());
        return bukuRepository.findByKodeBuku(query.getNomorBuku());
    }
}
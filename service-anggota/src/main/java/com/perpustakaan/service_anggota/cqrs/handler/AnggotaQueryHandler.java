package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.query.*;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnggotaQueryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaQueryHandler.class);
    
    @Autowired
    private AnggotaRepository anggotaRepository;
    
    @Transactional(readOnly = true)
    public Anggota handle(GetAnggotaByIdQuery query) {
        logger.debug("Handling GetAnggotaByIdQuery for ID: {}", query.getId());
        return anggotaRepository.findById(query.getId()).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Page<Anggota> handle(GetAllAnggotaQuery query) {
        logger.debug("Handling GetAllAnggotaQuery - page: {}, size: {}", query.getPage(), query.getSize());
        
        PageRequest pageRequest = PageRequest.of(
            query.getPage(), 
            query.getSize(), 
            Sort.by(query.getSortBy()).ascending()
        );
        
        return anggotaRepository.findAll(pageRequest);
    }
    
    @Transactional(readOnly = true)
    public Anggota handle(GetAnggotaByNomorQuery query) {
        logger.debug("Handling GetAnggotaByNomorQuery for nomor: {}", query.getNomorAnggota());
        return anggotaRepository.findByNomorAnggota(query.getNomorAnggota());
    }
}
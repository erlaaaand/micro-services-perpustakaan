package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.entity.command.Buku;
import com.perpustakaan.service_buku.event.BukuCreatedEvent;
import com.perpustakaan.service_buku.event.BukuDeletedEvent;
import com.perpustakaan.service_buku.event.BukuUpdatedEvent;
import com.perpustakaan.service_buku.repository.command.BukuCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class BukuCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BukuCommandHandler.class);
    
    @Autowired
    private BukuCommandRepository bukuRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher; // Ganti RabbitTemplate
    
    @Transactional("writeTransactionManager")
    public Buku handle(CreateBukuCommand command) {
        logger.info("Handling CreateBukuCommand for kode: {}", command.getKodeBuku());

        Buku existing = bukuRepository.findByKodeBuku(command.getKodeBuku());
        if (existing != null) {
            throw new IllegalArgumentException("Kode buku sudah digunakan: " + command.getKodeBuku());
        }
        
        Buku buku = new Buku();
        buku.setKodeBuku(command.getKodeBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPengarang());
        buku.setPenerbit(command.getPenerbit() != null ? command.getPenerbit() : "");
        buku.setTahunTerbit(command.getTahunTerbit());
        
        Buku saved = bukuRepository.save(buku);
        
        // Publish Internal Event
        publishBukuCreatedEvent(saved);
        return saved;
    }
    
    @Transactional("writeTransactionManager")
    public Buku handle(UpdateBukuCommand command) {
        logger.info("Handling UpdateBukuCommand for ID: {}", command.getId());
        
        Optional<Buku> existing = bukuRepository.findById(command.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }
        
        Buku buku = existing.get();

        if (!buku.getKodeBuku().equals(command.getKodeBuku())) {
            Buku conflict = bukuRepository.findByKodeBuku(command.getKodeBuku());
            if (conflict != null && !conflict.getId().equals(command.getId())) {
                throw new IllegalArgumentException("Kode buku sudah digunakan: " + command.getKodeBuku());
            }
        }

        buku.setKodeBuku(command.getKodeBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPengarang());
        buku.setPenerbit(command.getPenerbit() != null ? command.getPenerbit() : buku.getPenerbit());
        buku.setTahunTerbit(command.getTahunTerbit());
        
        Buku updated = bukuRepository.save(buku);
        
        publishBukuUpdatedEvent(updated);
        return updated;
    }
    
    @Transactional("writeTransactionManager")
    public void handle(DeleteBukuCommand command) {
        logger.info("Handling DeleteBukuCommand for ID: {}", command.getId());
        
        if (!bukuRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }

        bukuRepository.deleteById(command.getId());
        publishBukuDeletedEvent(command.getId());
    }
    
    private void publishBukuCreatedEvent(Buku buku) {
        BukuCreatedEvent event = new BukuCreatedEvent(
            buku.getId(),
            buku.getKodeBuku(),
            buku.getJudul(),
            buku.getPengarang(),
            buku.getPenerbit(),
            buku.getTahunTerbit()
        );
        eventPublisher.publishEvent(event);
        logger.info("Published Internal BukuCreatedEvent for ID: {}", buku.getId());
    }
    
    private void publishBukuUpdatedEvent(Buku buku) {
        BukuUpdatedEvent event = new BukuUpdatedEvent(
            buku.getId(),
            buku.getKodeBuku(),
            buku.getJudul(),
            buku.getPengarang(),
            buku.getPenerbit(),
            buku.getTahunTerbit()
        );
        eventPublisher.publishEvent(event);
        logger.info("Published Internal BukuUpdatedEvent for ID: {}", buku.getId());
    }
    
    private void publishBukuDeletedEvent(Long id) {
        BukuDeletedEvent event = new BukuDeletedEvent(id);
        eventPublisher.publishEvent(event);
        logger.info("Published Internal BukuDeletedEvent for ID: {}", id);
    }
}
package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.entity.Buku;
import com.perpustakaan.service_buku.event.BukuCreatedEvent;
import com.perpustakaan.service_buku.event.BukuDeletedEvent;
import com.perpustakaan.service_buku.event.BukuUpdatedEvent;
import com.perpustakaan.service_buku.repository.BukuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class BukuCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BukuCommandHandler.class);
    
    @Autowired
    private BukuRepository bukuRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.buku}")
    private String bukuExchange;
    
    @Value("${rabbitmq.routing-key.buku.created}")
    private String bukuCreatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.buku.updated}")
    private String bukuUpdatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.buku.deleted}")
    private String bukuDeletedRoutingKey;
    
    @Transactional
    public Buku handle(CreateBukuCommand command) {
        logger.info("Handling CreateBukuCommand for kode: {}", command.getKodeBuku());

        // Validasi duplikasi
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
        logger.info("Successfully created buku with ID: {}", saved.getId());
        
        publishBukuCreatedEvent(saved);
        return saved;
    }
    
    @Transactional
    public Buku handle(UpdateBukuCommand command) {
        logger.info("Handling UpdateBukuCommand for ID: {}", command.getId());
        
        Optional<Buku> existing = bukuRepository.findById(command.getId());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }
        
        Buku buku = existing.get();

        // Cek konflik kode buku jika berubah
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
        logger.info("Successfully updated buku with ID: {}", updated.getId());
        
        publishBukuUpdatedEvent(updated);
        return updated;
    }
    
    @Transactional
    public void handle(DeleteBukuCommand command) {
        logger.info("Handling DeleteBukuCommand for ID: {}", command.getId());
        
        if (!bukuRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }

        bukuRepository.deleteById(command.getId());
        logger.info("Successfully deleted buku with ID: {}", command.getId());
        
        publishBukuDeletedEvent(command.getId());
    }
    
    private void publishBukuCreatedEvent(Buku buku) {
        try {
            BukuCreatedEvent event = new BukuCreatedEvent(
                buku.getId(),
                buku.getKodeBuku(),
                buku.getJudul(),
                buku.getPengarang(),
                buku.getPenerbit(),
                buku.getTahunTerbit()
            );
            rabbitTemplate.convertAndSend(bukuExchange, bukuCreatedRoutingKey, event);
        } catch (Exception e) {
            logger.error("Failed to publish BukuCreatedEvent", e);
        }
    }
    
    private void publishBukuUpdatedEvent(Buku buku) {
        try {
            BukuUpdatedEvent event = new BukuUpdatedEvent(
                buku.getId(),
                buku.getKodeBuku(),
                buku.getJudul(),
                buku.getPengarang(),
                buku.getPenerbit(),
                buku.getTahunTerbit()
            );
            rabbitTemplate.convertAndSend(bukuExchange, bukuUpdatedRoutingKey, event);
        } catch (Exception e) {
            logger.error("Failed to publish BukuUpdatedEvent", e);
        }
    }
    
    private void publishBukuDeletedEvent(Long id) {
        try {
            BukuDeletedEvent event = new BukuDeletedEvent(id);
            rabbitTemplate.convertAndSend(bukuExchange, bukuDeletedRoutingKey, event);
        } catch (Exception e) {
            logger.error("Failed to publish BukuDeletedEvent", e);
        }
    }
}
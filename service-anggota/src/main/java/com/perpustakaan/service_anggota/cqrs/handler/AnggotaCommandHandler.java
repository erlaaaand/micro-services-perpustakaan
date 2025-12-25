package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.event.AnggotaCreatedEvent;
import com.perpustakaan.service_anggota.event.AnggotaDeletedEvent;
import com.perpustakaan.service_anggota.event.AnggotaUpdatedEvent;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AnggotaCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaCommandHandler.class);
    
    @Autowired
    private AnggotaRepository anggotaRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.anggota}")
    private String anggotaExchange;
    
    @Value("${rabbitmq.routing-key.anggota.created}")
    private String anggotaCreatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.anggota.updated}")
    private String anggotaUpdatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.anggota.deleted}")
    private String anggotaDeletedRoutingKey;
    
    @Transactional
    public Anggota handle(CreateAnggotaCommand command) {
        logger.info("Handling CreateAnggotaCommand for nomor: {}", command.getNomorAnggota());
        
        // Validasi duplikasi
        Anggota existing = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
        if (existing != null) {
            throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + command.getNomorAnggota());
        }
        
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        
        Anggota saved = anggotaRepository.save(anggota);
        logger.info("Successfully created anggota with ID: {}", saved.getId());
        
        // Publish event
        publishAnggotaCreatedEvent(saved);
        
        return saved;
    }
    
    @Transactional
    public Anggota handle(UpdateAnggotaCommand command) {
        logger.info("Handling UpdateAnggotaCommand for ID: {}", command.getId());
        
        Optional<Anggota> existing = anggotaRepository.findById(command.getId());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
        }
        
        Anggota anggota = existing.get();
        
        // Validasi nomor anggota jika berubah
        if (!anggota.getNomorAnggota().equals(command.getNomorAnggota())) {
            Anggota conflict = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
            if (conflict != null && !conflict.getId().equals(command.getId())) {
                throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + command.getNomorAnggota());
            }
        }
        
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        
        Anggota updated = anggotaRepository.save(anggota);
        logger.info("Successfully updated anggota with ID: {}", updated.getId());
        
        // Publish event
        publishAnggotaUpdatedEvent(updated);
        
        return updated;
    }
    
    @Transactional
    public void handle(DeleteAnggotaCommand command) {
        logger.info("Handling DeleteAnggotaCommand for ID: {}", command.getId());
        
        if (!anggotaRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
        }
        
        anggotaRepository.deleteById(command.getId());
        logger.info("Successfully deleted anggota with ID: {}", command.getId());
        
        // Publish event
        publishAnggotaDeletedEvent(command.getId());
    }
    
    private void publishAnggotaCreatedEvent(Anggota anggota) {
        try {
            AnggotaCreatedEvent event = new AnggotaCreatedEvent(
                anggota.getId(),
                anggota.getNomorAnggota(),
                anggota.getNama(),
                anggota.getAlamat(),
                anggota.getEmail()
            );
            rabbitTemplate.convertAndSend(anggotaExchange, anggotaCreatedRoutingKey, event);
            logger.info("Published AnggotaCreatedEvent for ID: {}", anggota.getId());
        } catch (Exception e) {
            logger.error("Failed to publish AnggotaCreatedEvent", e);
        }
    }
    
    private void publishAnggotaUpdatedEvent(Anggota anggota) {
        try {
            AnggotaUpdatedEvent event = new AnggotaUpdatedEvent(
                anggota.getId(),
                anggota.getNomorAnggota(),
                anggota.getNama(),
                anggota.getAlamat(),
                anggota.getEmail()
            );
            rabbitTemplate.convertAndSend(anggotaExchange, anggotaUpdatedRoutingKey, event);
            logger.info("Published AnggotaUpdatedEvent for ID: {}", anggota.getId());
        } catch (Exception e) {
            logger.error("Failed to publish AnggotaUpdatedEvent", e);
        }
    }
    
    private void publishAnggotaDeletedEvent(Long id) {
        try {
            AnggotaDeletedEvent event = new AnggotaDeletedEvent(id);
            rabbitTemplate.convertAndSend(anggotaExchange, anggotaDeletedRoutingKey, event);
            logger.info("Published AnggotaDeletedEvent for ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to publish AnggotaDeletedEvent", e);
        }
    }
}
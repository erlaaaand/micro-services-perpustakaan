package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.entity.command.Anggota;
import com.perpustakaan.service_anggota.event.AnggotaCreatedEvent;
import com.perpustakaan.service_anggota.event.AnggotaDeletedEvent;
import com.perpustakaan.service_anggota.event.AnggotaUpdatedEvent;
import com.perpustakaan.service_anggota.repository.command.AnggotaCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AnggotaCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaCommandHandler.class);
    
    @Autowired
    private AnggotaCommandRepository anggotaRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${perpustakaan.rabbitmq.exchange}")
    private String exchange;

    @Value("${perpustakaan.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public Anggota handle(CreateAnggotaCommand command) {
        logger.info("Handling CreateAnggotaCommand for nomor: {}", command.getNomorAnggota());
        
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
        
        // Publish Event Internal
        publishAnggotaCreatedEvent(saved);
        return saved;
    }
    
    @Transactional
    public Anggota handle(UpdateAnggotaCommand command) {
        // Logging
        logger.info("Handle RabbitMQ Event: Handling UpdateAnggotaCommand for ID: {}", command.getId());

         Optional<Anggota> existing = anggotaRepository.findById(command.getId());
         Anggota anggota = existing.get();
         anggota.setNomorAnggota(command.getNomorAnggota());
         anggota.setNama(command.getNama());
         anggota.setAlamat(command.getAlamat());
         anggota.setEmail(command.getEmail());
         Anggota updated = anggotaRepository.save(anggota);

         publishAnggotaUpdatedEvent(updated); // Panggil publish baru
         return updated;
    }

    @Transactional
    public void handle(DeleteAnggotaCommand command) {
        anggotaRepository.deleteById(command.getId());
        publishAnggotaDeletedEvent(command.getId());
    }

    private void publishAnggotaCreatedEvent(Anggota anggota) {
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(
            anggota.getId(),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail()
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: Created ID {}", anggota.getId());
    }
    
    private void publishAnggotaUpdatedEvent(Anggota anggota) {
        AnggotaUpdatedEvent event = new AnggotaUpdatedEvent(
            anggota.getId(),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail()
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: Updated ID {}", anggota.getId());
    }
    
    private void publishAnggotaDeletedEvent(Long id) {
        AnggotaDeletedEvent event = new AnggotaDeletedEvent(id);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: Deleted ID {}", id);
    }
}
package com.perpustakaan.service_pengembalian.cqrs.handler;

import com.perpustakaan.service_pengembalian.cqrs.command.*;
import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.event.*;
import com.perpustakaan.service_pengembalian.repository.PengembalianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PengembalianCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianCommandHandler.class);

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pengembalian}")
    private String pengembalianExchange;

    @Value("${rabbitmq.routing-key.pengembalian.created}")
    private String createdKey;
    
    @Value("${rabbitmq.routing-key.pengembalian.updated}")
    private String updatedKey;
    
    @Value("${rabbitmq.routing-key.pengembalian.deleted}")
    private String deletedKey;

    @Transactional
    public Pengembalian handle(CreatePengembalianCommand command) {
        logger.info("Handling CreatePengembalianCommand for Peminjaman ID: {}", command.getPeminjamanId());

        Pengembalian pengembalian = new Pengembalian();
        pengembalian.setPeminjamanId(command.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(command.getTanggalDikembalikan());
        pengembalian.setTerlambat(command.getTerlambat());
        pengembalian.setDenda(command.getDenda());

        Pengembalian saved = pengembalianRepository.save(pengembalian);
        
        // Publish Event
        publishEvent(new PengembalianCreatedEvent(saved.getId(), saved.getPeminjamanId(), saved.getTanggalDikembalikan(), saved.getDenda()), createdKey);
        
        return saved;
    }

    @Transactional
    public Pengembalian handle(UpdatePengembalianCommand command) {
        logger.info("Handling UpdatePengembalianCommand ID: {}", command.getId());

        Pengembalian existing = pengembalianRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pengembalian not found: " + command.getId()));

        existing.setPeminjamanId(command.getPeminjamanId());
        existing.setTanggalDikembalikan(command.getTanggalDikembalikan());
        existing.setTerlambat(command.getTerlambat());
        existing.setDenda(command.getDenda());

        Pengembalian updated = pengembalianRepository.save(existing);
        
        // Publish Event
        publishEvent(new PengembalianUpdatedEvent(updated.getId(), updated.getPeminjamanId(), updated.getDenda()), updatedKey);
        
        return updated;
    }

    @Transactional
    public void handle(DeletePengembalianCommand command) {
        logger.info("Handling DeletePengembalianCommand ID: {}", command.getId());

        if (!pengembalianRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Pengembalian not found: " + command.getId());
        }

        pengembalianRepository.deleteById(command.getId());
        
        // Publish Event
        publishEvent(new PengembalianDeletedEvent(command.getId()), deletedKey);
    }

    private void publishEvent(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(pengembalianExchange, routingKey, event);
            logger.info("Event published to RabbitMQ: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Failed to publish event", e);
        }
    }
}
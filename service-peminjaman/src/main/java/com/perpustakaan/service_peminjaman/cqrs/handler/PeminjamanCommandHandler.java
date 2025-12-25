package com.perpustakaan.service_peminjaman.cqrs.handler;

import com.perpustakaan.service_peminjaman.cqrs.command.*;
import com.perpustakaan.service_peminjaman.entity.Peminjaman;
import com.perpustakaan.service_peminjaman.event.PeminjamanCreatedEvent;
import com.perpustakaan.service_peminjaman.event.PeminjamanDeletedEvent;
import com.perpustakaan.service_peminjaman.event.PeminjamanUpdatedEvent;
import com.perpustakaan.service_peminjaman.repository.PeminjamanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PeminjamanCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanCommandHandler.class);

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.peminjaman}")
    private String peminjamanExchange;

    @Value("${rabbitmq.queue.peminjaman}") // Optional jika needed
    private String peminjamanQueue;

    private final String ROUTING_KEY_CREATED = "peminjaman.created";
    private final String ROUTING_KEY_UPDATED = "peminjaman.updated";
    private final String ROUTING_KEY_DELETED = "peminjaman.deleted";

    @Transactional
    public Peminjaman handle(CreatePeminjamanCommand command) {
        logger.info("Handling CreatePeminjamanCommand");

        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setAnggotaId(command.getAnggotaId());
        peminjaman.setBukuId(command.getBukuId());
        peminjaman.setTanggalPinjam(command.getTanggalPinjam());
        peminjaman.setTanggalKembali(command.getTanggalKembali());
        peminjaman.setStatus(command.getStatus());

        Peminjaman saved = peminjamanRepository.save(peminjaman);
        publishCreatedEvent(saved);
        
        return saved;
    }

    @Transactional
    public Peminjaman handle(UpdatePeminjamanCommand command) {
        logger.info("Handling UpdatePeminjamanCommand ID: {}", command.getId());

        Peminjaman peminjaman = peminjamanRepository.findById(command.getId())
            .orElseThrow(() -> new IllegalArgumentException("Peminjaman not found: " + command.getId()));

        peminjaman.setAnggotaId(command.getAnggotaId());
        peminjaman.setBukuId(command.getBukuId());
        peminjaman.setTanggalPinjam(command.getTanggalPinjam());
        peminjaman.setTanggalKembali(command.getTanggalKembali());
        peminjaman.setStatus(command.getStatus());

        Peminjaman updated = peminjamanRepository.save(peminjaman);
        publishUpdatedEvent(updated);
        
        return updated;
    }

    // Method baru untuk PATCH status
    @Transactional
    public Peminjaman handleUpdateStatus(Long id, String status) {
        logger.info("Handling Update Status ID: {}", id);

        Peminjaman peminjaman = peminjamanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Peminjaman not found: " + id));

        peminjaman.setStatus(status);

        Peminjaman updated = peminjamanRepository.save(peminjaman);
        publishUpdatedEvent(updated);
        
        return updated;
    }

    @Transactional
    public void handle(DeletePeminjamanCommand command) {
        logger.info("Handling DeletePeminjamanCommand ID: {}", command.getId());

        if (!peminjamanRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Peminjaman not found: " + command.getId());
        }

        peminjamanRepository.deleteById(command.getId());
        publishDeletedEvent(command.getId());
    }

    // --- Events Publishers ---

    private void publishCreatedEvent(Peminjaman peminjaman) {
        try {
            PeminjamanCreatedEvent event = new PeminjamanCreatedEvent(
                peminjaman.getId(),
                peminjaman.getAnggotaId(),
                peminjaman.getBukuId(),
                peminjaman.getTanggalPinjam(),
                peminjaman.getStatus()
            );
            rabbitTemplate.convertAndSend(peminjamanExchange, ROUTING_KEY_CREATED, event);
        } catch (Exception e) {
            logger.error("Failed to publish Created Event", e);
        }
    }

    private void publishUpdatedEvent(Peminjaman peminjaman) {
        try {
            PeminjamanUpdatedEvent event = new PeminjamanUpdatedEvent(
                peminjaman.getId(),
                peminjaman.getStatus(),
                peminjaman.getTanggalKembali()
            );
            rabbitTemplate.convertAndSend(peminjamanExchange, ROUTING_KEY_UPDATED, event);
        } catch (Exception e) {
            logger.error("Failed to publish Updated Event", e);
        }
    }

    private void publishDeletedEvent(Long id) {
        try {
            PeminjamanDeletedEvent event = new PeminjamanDeletedEvent(id);
            rabbitTemplate.convertAndSend(peminjamanExchange, ROUTING_KEY_DELETED, event);
        } catch (Exception e) {
            logger.error("Failed to publish Deleted Event", e);
        }
    }
}
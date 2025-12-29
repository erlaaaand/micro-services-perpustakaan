package com.perpustakaan.service_peminjaman.cqrs.handler;

import com.perpustakaan.service_peminjaman.cqrs.command.*;
import com.perpustakaan.service_peminjaman.entity.command.Peminjaman;
import com.perpustakaan.service_peminjaman.event.PeminjamanCreatedEvent;
import com.perpustakaan.service_peminjaman.event.PeminjamanDeletedEvent;
import com.perpustakaan.service_peminjaman.event.PeminjamanUpdatedEvent;
import com.perpustakaan.service_peminjaman.repository.command.PeminjamanRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PeminjamanCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanCommandHandler.class);

    @Autowired
    private PeminjamanRepository peminjamanRepository; // JPA Write Repo

    @Autowired
    private ApplicationEventPublisher eventPublisher; // Internal Event

    @Transactional("writeTransactionManager")
    public Peminjaman handle(CreatePeminjamanCommand command) {
        logger.info("Handling CreatePeminjamanCommand");

        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setAnggotaId(command.getAnggotaId());
        peminjaman.setBukuId(command.getBukuId());
        peminjaman.setTanggalPinjam(command.getTanggalPinjam());
        peminjaman.setTanggalKembali(command.getTanggalKembali());
        peminjaman.setStatus(command.getStatus());

        Peminjaman saved = peminjamanRepository.save(peminjaman);
        
        // Publish Internal Event
        publishCreatedEvent(saved);
        
        return saved;
    }

    @Transactional("writeTransactionManager")
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

    @Transactional("writeTransactionManager")
    public Peminjaman handleUpdateStatus(Long id, String status) {
        logger.info("Handling Update Status ID: {}", id);

        Peminjaman peminjaman = peminjamanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Peminjaman not found: " + id));

        peminjaman.setStatus(status);

        Peminjaman updated = peminjamanRepository.save(peminjaman);
        publishUpdatedEvent(updated);
        return updated;
    }

    @Transactional("writeTransactionManager")
    public void handle(DeletePeminjamanCommand command) {
        logger.info("Handling DeletePeminjamanCommand ID: {}", command.getId());

        if (!peminjamanRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Peminjaman not found: " + command.getId());
        }

        peminjamanRepository.deleteById(command.getId());
        publishDeletedEvent(command.getId());
    }

    // --- Internal Events ---

    private void publishCreatedEvent(Peminjaman peminjaman) {
        PeminjamanCreatedEvent event = new PeminjamanCreatedEvent(
            peminjaman.getId(),
            peminjaman.getAnggotaId(),
            peminjaman.getBukuId(),
            peminjaman.getTanggalPinjam(),
            peminjaman.getStatus()
        );
        eventPublisher.publishEvent(event);
    }

    private void publishUpdatedEvent(Peminjaman peminjaman) {
        PeminjamanUpdatedEvent event = new PeminjamanUpdatedEvent(
            peminjaman.getId(),
            peminjaman.getStatus(),
            peminjaman.getTanggalKembali()
        );
        eventPublisher.publishEvent(event);
    }

    private void publishDeletedEvent(Long id) {
        PeminjamanDeletedEvent event = new PeminjamanDeletedEvent(id);
        eventPublisher.publishEvent(event);
    }
}
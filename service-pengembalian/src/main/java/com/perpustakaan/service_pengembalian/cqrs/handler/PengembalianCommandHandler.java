package com.perpustakaan.service_pengembalian.cqrs.handler;

import com.perpustakaan.service_pengembalian.cqrs.command.*;
import com.perpustakaan.service_pengembalian.entity.command.Pengembalian;
import com.perpustakaan.service_pengembalian.event.*;
import com.perpustakaan.service_pengembalian.repository.command.PengembalianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PengembalianCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianCommandHandler.class);

    @Autowired
    private PengembalianRepository pengembalianRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional("writeTransactionManager")
    public Pengembalian handle(CreatePengembalianCommand command) {
        logger.info("Handling CreatePengembalianCommand for Peminjaman ID: {}", command.getPeminjamanId());

        Pengembalian pengembalian = new Pengembalian();
        pengembalian.setPeminjamanId(command.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(command.getTanggalDikembalikan());
        pengembalian.setTerlambat(command.getTerlambat());
        pengembalian.setDenda(command.getDenda());

        Pengembalian saved = pengembalianRepository.save(pengembalian);
        
        publishCreatedEvent(saved);
        return saved;
    }

    @Transactional("writeTransactionManager")
    public Pengembalian handle(UpdatePengembalianCommand command) {
        logger.info("Handling UpdatePengembalianCommand ID: {}", command.getId());

        Pengembalian existing = pengembalianRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pengembalian not found: " + command.getId()));

        existing.setPeminjamanId(command.getPeminjamanId());
        existing.setTanggalDikembalikan(command.getTanggalDikembalikan());
        existing.setTerlambat(command.getTerlambat());
        existing.setDenda(command.getDenda());

        Pengembalian updated = pengembalianRepository.save(existing);
        
        publishUpdatedEvent(updated);
        return updated;
    }

    @Transactional("writeTransactionManager")
    public void handle(DeletePengembalianCommand command) {
        logger.info("Handling DeletePengembalianCommand ID: {}", command.getId());

        if (!pengembalianRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Pengembalian not found: " + command.getId());
        }

        pengembalianRepository.deleteById(command.getId());
        
        publishDeletedEvent(command.getId());
    }

    private void publishCreatedEvent(Pengembalian p) {
        PengembalianCreatedEvent event = new PengembalianCreatedEvent(p.getId(), p.getPeminjamanId(), p.getTanggalDikembalikan(), p.getDenda());
        eventPublisher.publishEvent(event);
    }

    private void publishUpdatedEvent(Pengembalian p) {
        PengembalianUpdatedEvent event = new PengembalianUpdatedEvent(p.getId(), p.getPeminjamanId(), p.getDenda());
        eventPublisher.publishEvent(event);
    }

    private void publishDeletedEvent(Long id) {
        PengembalianDeletedEvent event = new PengembalianDeletedEvent(id);
        eventPublisher.publishEvent(event);
    }
}
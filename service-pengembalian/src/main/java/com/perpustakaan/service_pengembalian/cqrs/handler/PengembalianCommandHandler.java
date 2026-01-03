package com.perpustakaan.service_pengembalian.cqrs.handler;

import com.perpustakaan.service_pengembalian.cqrs.command.*;
import com.perpustakaan.service_pengembalian.entity.command.Pengembalian;
import com.perpustakaan.service_pengembalian.event.*;
import com.perpustakaan.service_pengembalian.repository.command.PengembalianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Value("${perpustakaan.rabbitmq.exchange}")
    private String exchange;

    @Value("${perpustakaan.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public Pengembalian handle(CreatePengembalianCommand command) {
        logger.info("Handle RabbitMQ Event: Handling CreatePengembalianCommand for Peminjaman ID: {}", command.getPeminjamanId());

        Pengembalian pengembalian = new Pengembalian();
        pengembalian.setPeminjamanId(command.getPeminjamanId());
        pengembalian.setTanggalDikembalikan(command.getTanggalDikembalikan());
        pengembalian.setTerlambat(command.getTerlambat());
        pengembalian.setDenda(command.getDenda());

        Pengembalian saved = pengembalianRepository.save(pengembalian);
        
        publishCreatedEvent(saved);
        return saved;
    }

    @Transactional
    public Pengembalian handle(UpdatePengembalianCommand command) {
        logger.info("Handle RabbitMQ Event: Handling UpdatePengembalianCommand ID: {}", command.getId());

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

    @Transactional
    public void handle(DeletePengembalianCommand command) {
        logger.info("Handle RabbitMQ Event: Handling DeletePengembalianCommand ID: {}", command.getId());

        if (!pengembalianRepository.existsById(command.getId())) {
            throw new IllegalArgumentException("Pengembalian not found: " + command.getId());
        }

        pengembalianRepository.deleteById(command.getId());
        
        publishDeletedEvent(command.getId());
    }

    private void publishCreatedEvent(Pengembalian p) {
        PengembalianCreatedEvent event = new PengembalianCreatedEvent(p.getId(), p.getPeminjamanId(), p.getTanggalDikembalikan(), p.getDenda());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: Internal PengembalianCreatedEvent for ID: {}", p.getId());
    }

    private void publishUpdatedEvent(Pengembalian p) {
        PengembalianUpdatedEvent event = new PengembalianUpdatedEvent(p.getId(), p.getPeminjamanId(), p.getDenda());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: PengembalianUpdatedEvent for ID: {}", p.getId());
    }

    private void publishDeletedEvent(Long id) {
        PengembalianDeletedEvent event = new PengembalianDeletedEvent(id);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        logger.info("Published RabbitMQ Event: PengembalianDeletedEvent for ID: {}", id);
    }
}
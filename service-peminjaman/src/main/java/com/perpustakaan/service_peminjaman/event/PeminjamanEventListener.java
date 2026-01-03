package com.perpustakaan.service_peminjaman.event;

import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import com.perpustakaan.service_peminjaman.repository.query.PeminjamanQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
public class PeminjamanEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanEventListener.class);

    @Autowired
    private PeminjamanQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(PeminjamanCreatedEvent event) {
        logger.info("RabbitMQ Listener: Created Peminjaman to MongoDB: {}", event.getId());
        PeminjamanReadModel model = new PeminjamanReadModel();
        model.setId(event.getId());
        model.setAnggotaId(event.getAnggotaId());
        model.setBukuId(event.getBukuId());
        model.setTanggalPinjam(event.getTanggalPinjam());
        // Default value atau logic lain jika null
        model.setStatus(event.getStatus()); 
        
        queryRepository.save(model);
        logger.info("RabbitMQ Listener: Successfully synced ID {}", event.getId());
    }

    @RabbitHandler
    public void handleUpdatedEvent(PeminjamanUpdatedEvent event) {
        logger.info("RabbitMQ Listener: Updated Peminjaman to MongoDB: {}", event.getId());

        PeminjamanReadModel model = queryRepository.findById(event.getId())
            .orElse(new PeminjamanReadModel());
        

        model.setStatus(event.getStatus());
        model.setTanggalKembali(null);
        
        queryRepository.save(model);

        logger.info("RabbitMQ Listener: Successfully synced ID {}", event.getId());
    }

    @RabbitHandler
    public void handleDeletedEvent(PeminjamanDeletedEvent event) {
        logger.info("RabbitMQ Listener: Deleted Peminjaman from MongoDB: {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        } else {
            logger.warn("RabbitMQ Listener: Peminjaman ID {} not found for deletion", event.getId());
        }

        logger.info("RabbitMQ Listener: Successfully deleted ID {}", event.getId());
    }
}
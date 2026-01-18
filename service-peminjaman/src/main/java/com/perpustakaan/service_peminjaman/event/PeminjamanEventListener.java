package com.perpustakaan.service_peminjaman.event;

import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import com.perpustakaan.service_peminjaman.repository.query.PeminjamanQueryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
@RequiredArgsConstructor
public class PeminjamanEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanEventListener.class);
    private final PeminjamanQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(PeminjamanCreatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Sync Create Peminjaman ID [{}] ke MongoDB", idString);
        
        PeminjamanReadModel model = new PeminjamanReadModel();
        model.setId(idString);
        // Konversi UUID -> String
        model.setAnggotaId(event.getAnggotaId().toString());
        model.setBukuId(event.getBukuId().toString());
        model.setTanggalPinjam(event.getTanggalPinjam());
        model.setTanggalKembali(null); // Default null saat created (bisa disesuaikan jika field ada di event)
        model.setStatus(event.getStatus()); 
        
        queryRepository.save(model);
        logger.debug("RabbitMQ Listener: Data tersimpan di Read DB.");
    }

    @RabbitHandler
    public void handleUpdatedEvent(PeminjamanUpdatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Sync Update Peminjaman ID [{}] ke MongoDB", idString);

        PeminjamanReadModel model = queryRepository.findById(idString)
            .orElse(new PeminjamanReadModel());
        
        // Update field yang relevan
        model.setId(idString); 
        model.setStatus(event.getStatus());
        model.setTanggalKembali(event.getTanggalKembali());
        
        queryRepository.save(model);
    }

    @RabbitHandler
    public void handleDeletedEvent(PeminjamanDeletedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Sync Delete Peminjaman ID [{}]", idString);
        
        if (queryRepository.existsById(idString)) {
            queryRepository.deleteById(idString);
        } else {
            logger.warn("RabbitMQ Listener: ID [{}] tidak ditemukan di MongoDB.", idString);
        }
    }
}
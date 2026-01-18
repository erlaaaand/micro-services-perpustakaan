package com.perpustakaan.service_pengembalian.event;

import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import com.perpustakaan.service_pengembalian.repository.query.PengembalianQueryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
@RequiredArgsConstructor
public class PengembalianEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianEventListener.class);
    private final PengembalianQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(PengembalianCreatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Created Pengembalian to MongoDB: {}", idString);
        
        PengembalianReadModel model = new PengembalianReadModel();
        model.setId(idString);
        // FIX: Gunakan getPeminjamanId() dari event, bukan ID pengembalian
        model.setPeminjamanId(event.getPeminjamanId().toString()); 
        model.setTanggalDikembalikan(event.getTanggalDikembalikan());
        model.setTerlambat(event.getTerlambat());
        model.setDenda(event.getDenda());
        
        queryRepository.save(model);
        logger.info("RabbitMQ Listener: Successfully synced ID {}", idString);
    }

    @RabbitHandler
    public void handleUpdatedEvent(PengembalianUpdatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Updated Pengembalian to MongoDB: {}", idString);
        
        PengembalianReadModel model = queryRepository.findById(idString)
            .orElse(new PengembalianReadModel());

        model.setId(idString); // Re-set ID
        model.setPeminjamanId(event.getPeminjamanId().toString());
        model.setTanggalDikembalikan(event.getTanggalDikembalikan());
        model.setTerlambat(event.getTerlambat());
        model.setDenda(event.getDenda());
        
        queryRepository.save(model);
        logger.info("RabbitMQ Listener: Successfully synced ID {}", idString);
    }

    @RabbitHandler
    public void handleDeletedEvent(PengembalianDeletedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Listener: Deleted Pengembalian from MongoDB: {}", idString);
        
        if (queryRepository.existsById(idString)) {
            queryRepository.deleteById(idString);
        } else {
            logger.warn("RabbitMQ Listener: ID {} not found", idString);
        }
    }
}
package com.perpustakaan.service_pengembalian.event;

import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import com.perpustakaan.service_pengembalian.repository.query.PengembalianQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
public class PengembalianEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianEventListener.class);

    @Autowired
    private PengembalianQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(PengembalianCreatedEvent event) {
        logger.info("RabbitMQ Listener: Created Pengembalian to MongoDB: {}", event.getId());
        PengembalianReadModel model = new PengembalianReadModel();
        model.setId(event.getId());
        model.setPeminjamanId(event.getPeminjamanId());
        model.setTanggalDikembalikan(event.getTanggalDikembalikan());
        model.setDenda(event.getDenda());
        // setTerlambat bisa ditambahkan di event jika diperlukan, atau diambil dari logic
        
        queryRepository.save(model);
    }

    @RabbitHandler
    public void handleUpdatedEvent(PengembalianUpdatedEvent event) {
        logger.info("RabbitMQ Listener: Updated Pengembalian to MongoDB: {}", event.getId());

        PengembalianReadModel model = queryRepository.findById(event.getId())
            .orElse(new PengembalianReadModel());

        model.setPeminjamanId(event.getPeminjamanId());
        model.setDenda(event.getDenda());
        queryRepository.save(model);

        logger.info("RabbitMQ Listener: Updated Pengembalian in MongoDB: {}", event.getId());
    }

    @RabbitHandler
    public void handleDeletedEvent(PengembalianDeletedEvent event) {
        logger.info("RabbitMQ Listener: Deleted Pengembalian from MongoDB: {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        } else {
            logger.warn("RabbitMQ Listener: Pengembalian with id {} not found in MongoDB for deletion", event.getId());
        }

        logger.info("RabbitMQ Listener: Deleted Pengembalian from MongoDB: {}", event.getId());
    }
}
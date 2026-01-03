package com.perpustakaan.service_buku.event;

import com.perpustakaan.service_buku.entity.query.BukuReadModel;
import com.perpustakaan.service_buku.repository.query.BukuQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
public class BukuEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BukuEventListener.class);

    @Autowired
    private BukuQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(BukuCreatedEvent event) {
        logger.info("RabbitMQ Receiver: Create Buku {}", event.getKodeBuku());
        BukuReadModel model = new BukuReadModel();
        model.setId(event.getId());
        model.setKodeBuku(event.getKodeBuku());
        model.setJudul(event.getJudul());
        model.setPengarang(event.getPengarang());
        model.setPenerbit(event.getPenerbit());
        model.setTahunTerbit(event.getTahunTerbit());
        
        queryRepository.save(model);
        logger.info("RabbitMQ Receiver: Successfully synced Buku {}", event.getKodeBuku());
    }

    @RabbitHandler
    public void handleUpdatedEvent(BukuUpdatedEvent event) {
        logger.info("RabbitMQ Receiver: Update Buku {}", event.getId());
        
        BukuReadModel model = queryRepository.findById(event.getId())
            .orElse(new BukuReadModel());

        model.setId(event.getId());
        model.setKodeBuku(event.getKodeBuku());
        model.setJudul(event.getJudul());
        model.setPengarang(event.getPengarang());
        model.setPenerbit(event.getPenerbit());
        model.setTahunTerbit(event.getTahunTerbit());

        queryRepository.save(model);

        logger.info("RabbitMQ Receiver: Successfully synced ID {}", event.getId());

    }

    @RabbitHandler
    public void handleDeletedEvent(BukuDeletedEvent event) {
        logger.info("RabbitMQ Receiver: Delete Buku {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        } else {
            logger.warn("RabbitMQ Receiver: Buku ID {} not found for deletion", event.getId());
        }
        logger.info("RabbitMQ Receiver: Successfully deleted ID {}", event.getId());
    }
}
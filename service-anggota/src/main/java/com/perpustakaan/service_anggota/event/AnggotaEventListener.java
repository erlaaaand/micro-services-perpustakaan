package com.perpustakaan.service_anggota.event;

import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel;
import com.perpustakaan.service_anggota.repository.query.AnggotaQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
public class AnggotaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaEventListener.class);

    @Autowired
    private AnggotaQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(AnggotaCreatedEvent event) {
        logger.info("RabbitMQ Receiver: Create Anggota ID {}", event.getId());
        
        AnggotaReadModel model = queryRepository.findById(event.getId())
            .orElse(new AnggotaReadModel());

        model.setId(event.getId());
        model.setNomorAnggota(event.getNomorAnggota());
        model.setNama(event.getNama());
        model.setAlamat(event.getAlamat());
        model.setEmail(event.getEmail());
        
        queryRepository.save(model);

        logger.info("RabbitMQ Receiver: Successfully synced ID {}", event.getId());
    }

    @RabbitHandler
    public void handleUpdatedEvent(AnggotaUpdatedEvent event) {
        logger.info("RabbitMQ Receiver: Update Anggota ID {}", event.getId());

        AnggotaReadModel model = queryRepository.findById(event.getId())
            .orElse(new AnggotaReadModel());

        model.setId(event.getId());

        model.setNomorAnggota(event.getNomorAnggota());
        model.setNama(event.getNama());
        model.setAlamat(event.getAlamat());
        model.setEmail(event.getEmail());

        queryRepository.save(model);

        logger.info("RabbitMQ Receiver: Successfully synced ID {}", event.getId());
    }

    @RabbitHandler
    public void handleDeletedEvent(AnggotaDeletedEvent event) {
        logger.info("RabbitMQ Receiver: Delete Anggota ID {}", event.getId());  
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        } else {
            logger.warn("RabbitMQ Receiver: Anggota ID {} not found for deletion", event.getId());
        }
        logger.info("RabbitMQ Receiver: Successfully deleted ID {}", event.getId());
    }
}
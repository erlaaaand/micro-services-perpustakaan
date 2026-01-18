package com.perpustakaan.service_anggota.event;

import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel;
import com.perpustakaan.service_anggota.repository.query.AnggotaQueryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${perpustakaan.rabbitmq.queue}")
@RequiredArgsConstructor
public class AnggotaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaEventListener.class);
    private final AnggotaQueryRepository queryRepository;

    @RabbitHandler
    public void handleCreatedEvent(AnggotaCreatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Receiver: Create Anggota ID {}", idString);
        
        AnggotaReadModel model = new AnggotaReadModel();
        model.setId(idString);
        model.setNomorAnggota(event.getNomorAnggota());
        model.setNama(event.getNama());
        model.setAlamat(event.getAlamat());
        model.setEmail(event.getEmail());
        
        queryRepository.save(model);
        logger.info("RabbitMQ Receiver: Saved to MongoDB ID {}", idString);
    }

    @RabbitHandler
    public void handleUpdatedEvent(AnggotaUpdatedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Receiver: Update Anggota ID {}", idString);

        AnggotaReadModel model = queryRepository.findById(idString)
            .orElse(new AnggotaReadModel());

        model.setId(idString);
        model.setNomorAnggota(event.getNomorAnggota());
        model.setNama(event.getNama());
        model.setAlamat(event.getAlamat());
        model.setEmail(event.getEmail());

        queryRepository.save(model);
        logger.info("RabbitMQ Receiver: Updated MongoDB ID {}", idString);
    }

    @RabbitHandler
    public void handleDeletedEvent(AnggotaDeletedEvent event) {
        String idString = event.getId().toString();
        logger.info("RabbitMQ Receiver: Delete Anggota ID {}", idString);  
        
        if (queryRepository.existsById(idString)) {
            queryRepository.deleteById(idString);
            logger.info("RabbitMQ Receiver: Deleted from MongoDB ID {}", idString);
        } else {
            logger.warn("RabbitMQ Receiver: ID {} not found in MongoDB", idString);
        }
    }
}
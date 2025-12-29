package com.perpustakaan.service_anggota.event;

import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel;
import com.perpustakaan.service_anggota.repository.query.AnggotaQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener; // Import Baru
import org.springframework.stereotype.Component;

@Component
public class AnggotaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaEventListener.class);

    @Autowired
    private AnggotaQueryRepository queryRepository;

    @EventListener // Ganti RabbitListener
    // @Async // Uncomment jika ingin sinkronisasi berjalan di thread terpisah (perlu @EnableAsync di main class)
    public void handleCreatedEvent(AnggotaCreatedEvent event) {
        logger.info("Syncing Created Anggota to MongoDB (Internal Event): {}", event.getId());
        
        AnggotaReadModel model = new AnggotaReadModel();
        model.setId(event.getId());
        model.setNomorAnggota(event.getNomorAnggota());
        model.setNama(event.getNama());
        model.setAlamat(event.getAlamat());
        model.setEmail(event.getEmail());
        
        queryRepository.save(model);
    }

    @EventListener
    public void handleUpdatedEvent(AnggotaUpdatedEvent event) {
        logger.info("Syncing Updated Anggota to MongoDB (Internal Event): {}", event.getId());
        queryRepository.findById(event.getId()).ifPresent(model -> {
            model.setNomorAnggota(event.getNomorAnggota());
            model.setNama(event.getNama());
            model.setAlamat(event.getAlamat());
            model.setEmail(event.getEmail());
            queryRepository.save(model);
        });
    }

    @EventListener
    public void handleDeletedEvent(AnggotaDeletedEvent event) {
        logger.info("Syncing Deleted Anggota from MongoDB (Internal Event): {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        }
    }
}
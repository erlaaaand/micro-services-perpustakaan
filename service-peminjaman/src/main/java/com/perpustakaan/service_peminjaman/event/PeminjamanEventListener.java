package com.perpustakaan.service_peminjaman.event;

import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import com.perpustakaan.service_peminjaman.repository.query.PeminjamanQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PeminjamanEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanEventListener.class);

    @Autowired
    private PeminjamanQueryRepository queryRepository;

    @EventListener
    public void handleCreatedEvent(PeminjamanCreatedEvent event) {
        logger.info("Syncing Created Peminjaman to MongoDB: {}", event.getId());
        PeminjamanReadModel model = new PeminjamanReadModel();
        model.setId(event.getId());
        model.setAnggotaId(event.getAnggotaId());
        model.setBukuId(event.getBukuId());
        model.setTanggalPinjam(event.getTanggalPinjam());
        // Default value atau logic lain jika null
        model.setStatus(event.getStatus()); 
        
        queryRepository.save(model);
    }

    @EventListener
    public void handleUpdatedEvent(PeminjamanUpdatedEvent event) {
        logger.info("Syncing Updated Peminjaman to MongoDB: {}", event.getId());
        queryRepository.findById(event.getId()).ifPresent(model -> {
            model.setStatus(event.getStatus());
            model.setTanggalKembali(event.getTanggalKembali());
            queryRepository.save(model);
        });
    }

    @EventListener
    public void handleDeletedEvent(PeminjamanDeletedEvent event) {
        logger.info("Syncing Deleted Peminjaman from MongoDB: {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        }
    }
}
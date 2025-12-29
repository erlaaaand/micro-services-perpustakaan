package com.perpustakaan.service_pengembalian.event;

import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import com.perpustakaan.service_pengembalian.repository.query.PengembalianQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PengembalianEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianEventListener.class);

    @Autowired
    private PengembalianQueryRepository queryRepository;

    @EventListener
    public void handleCreatedEvent(PengembalianCreatedEvent event) {
        logger.info("Syncing Created Pengembalian to MongoDB: {}", event.getId());
        PengembalianReadModel model = new PengembalianReadModel();
        model.setId(event.getId());
        model.setPeminjamanId(event.getPeminjamanId());
        model.setTanggalDikembalikan(event.getTanggalDikembalikan());
        model.setDenda(event.getDenda());
        // setTerlambat bisa ditambahkan di event jika diperlukan, atau diambil dari logic
        
        queryRepository.save(model);
    }

    @EventListener
    public void handleUpdatedEvent(PengembalianUpdatedEvent event) {
        logger.info("Syncing Updated Pengembalian to MongoDB: {}", event.getId());
        queryRepository.findById(event.getId()).ifPresent(model -> {
            model.setPeminjamanId(event.getPeminjamanId());
            model.setDenda(event.getDenda());
            queryRepository.save(model);
        });
    }

    @EventListener
    public void handleDeletedEvent(PengembalianDeletedEvent event) {
        logger.info("Syncing Deleted Pengembalian from MongoDB: {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        }
    }
}
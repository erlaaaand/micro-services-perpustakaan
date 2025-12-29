package com.perpustakaan.service_buku.event;

import com.perpustakaan.service_buku.entity.query.BukuReadModel;
import com.perpustakaan.service_buku.repository.query.BukuQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BukuEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BukuEventListener.class);

    @Autowired
    private BukuQueryRepository queryRepository;

    @EventListener
    public void handleCreatedEvent(BukuCreatedEvent event) {
        logger.info("Syncing Created Buku to MongoDB: {}", event.getKodeBuku());
        BukuReadModel model = new BukuReadModel();
        model.setId(event.getId());
        model.setKodeBuku(event.getKodeBuku());
        model.setJudul(event.getJudul());
        model.setPengarang(event.getPengarang());
        model.setPenerbit(event.getPenerbit());
        model.setTahunTerbit(event.getTahunTerbit());
        
        queryRepository.save(model);
    }

    @EventListener
    public void handleUpdatedEvent(BukuUpdatedEvent event) {
        logger.info("Syncing Updated Buku to MongoDB: {}", event.getId());
        queryRepository.findById(event.getId()).ifPresent(model -> {
            model.setKodeBuku(event.getKodeBuku());
            model.setJudul(event.getJudul());
            model.setPengarang(event.getPengarang());
            model.setPenerbit(event.getPenerbit());
            model.setTahunTerbit(event.getTahunTerbit());
            queryRepository.save(model);
        });
    }

    @EventListener
    public void handleDeletedEvent(BukuDeletedEvent event) {
        logger.info("Syncing Deleted Buku from MongoDB: {}", event.getId());
        if (queryRepository.existsById(event.getId())) {
            queryRepository.deleteById(event.getId());
        }
    }
}
package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.entity.command.Anggota;
import com.perpustakaan.service_anggota.event.AnggotaCreatedEvent;
import com.perpustakaan.service_anggota.event.AnggotaDeletedEvent;
import com.perpustakaan.service_anggota.event.AnggotaUpdatedEvent;
import com.perpustakaan.service_anggota.repository.command.AnggotaCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher; // Import Baru
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AnggotaCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaCommandHandler.class);
    
    @Autowired
    private AnggotaCommandRepository anggotaRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher; // Ganti RabbitTemplate
    
    // Hapus @Value RabbitMQ exchange/routing keys karena tidak dipakai lagi

    @Transactional
    public Anggota handle(CreateAnggotaCommand command) {
        logger.info("Handling CreateAnggotaCommand for nomor: {}", command.getNomorAnggota());
        
        // ... (Validasi & Logika simpan ke Repo Write TETAP SAMA) ...
        Anggota existing = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
        if (existing != null) {
            throw new IllegalArgumentException("Nomor anggota sudah digunakan: " + command.getNomorAnggota());
        }
        
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());

        Anggota saved = anggotaRepository.save(anggota);
        
        // Publish Event Internal
        publishAnggotaCreatedEvent(saved);
        return saved;
    }
    
    // ... (Handle Update & Delete TETAP SAMA logika simpannya) ...
    @Transactional
    public Anggota handle(UpdateAnggotaCommand command) {
         // ... logika update ...
         // contoh singkat:
         Optional<Anggota> existing = anggotaRepository.findById(command.getId());
         Anggota anggota = existing.get();
         anggota.setNomorAnggota(command.getNomorAnggota());
         anggota.setNama(command.getNama());
         anggota.setAlamat(command.getAlamat());
         anggota.setEmail(command.getEmail());
         Anggota updated = anggotaRepository.save(anggota);

         publishAnggotaUpdatedEvent(updated); // Panggil publish baru
         return updated;
    }

    @Transactional
    public void handle(DeleteAnggotaCommand command) {
        // ... logika delete ...
        anggotaRepository.deleteById(command.getId());
        publishAnggotaDeletedEvent(command.getId());
    }

    // --- METHOD PUBLISH YANG DIUBAH ---

    private void publishAnggotaCreatedEvent(Anggota anggota) {
        AnggotaCreatedEvent event = new AnggotaCreatedEvent(
            anggota.getId(),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail()
        );
        eventPublisher.publishEvent(event); // Kirim ke Internal Listener
        logger.info("Published Internal AnggotaCreatedEvent for ID: {}", anggota.getId());
    }
    
    private void publishAnggotaUpdatedEvent(Anggota anggota) {
        AnggotaUpdatedEvent event = new AnggotaUpdatedEvent(
            anggota.getId(),
            anggota.getNomorAnggota(),
            anggota.getNama(),
            anggota.getAlamat(),
            anggota.getEmail()
        );
        eventPublisher.publishEvent(event);
        logger.info("Published Internal AnggotaUpdatedEvent for ID: {}", anggota.getId());
    }
    
    private void publishAnggotaDeletedEvent(Long id) {
        AnggotaDeletedEvent event = new AnggotaDeletedEvent(id);
        eventPublisher.publishEvent(event);
        logger.info("Published Internal AnggotaDeletedEvent for ID: {}", id);
    }
}
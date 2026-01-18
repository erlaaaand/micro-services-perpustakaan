package com.perpustakaan.service_anggota.cqrs.handler;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.entity.command.AnggotaWriteModel;
import com.perpustakaan.service_anggota.event.*;
import com.perpustakaan.service_anggota.repository.command.AnggotaCommandRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnggotaCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnggotaCommandHandler.class);
    
    private final AnggotaCommandRepository anggotaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${perpustakaan.rabbitmq.exchange}")
    private String exchange;

    @Value("${perpustakaan.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public AnggotaWriteModel handle(CreateAnggotaCommand command) {
        logger.info("PROSES CREATE: Memulai pendaftaran anggota baru. Nomor: [{}], Email: [{}]", 
            command.getNomorAnggota(), command.getEmail());

        if (anggotaRepository.existsByNomorAnggota(command.getNomorAnggota())) {
            logger.warn("GAGAL CREATE: Nomor anggota [{}] sudah terdaftar di sistem.", command.getNomorAnggota());
            throw new IllegalArgumentException("Nomor anggota " + command.getNomorAnggota() + " sudah digunakan.");
        }

        if (anggotaRepository.existsByEmail(command.getEmail())) {
            logger.warn("GAGAL CREATE: Email [{}] sudah terdaftar di sistem.", command.getEmail());
            throw new IllegalArgumentException("Email " + command.getEmail() + " sudah digunakan.");
        }
        
        AnggotaWriteModel anggota = new AnggotaWriteModel();
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());

        AnggotaWriteModel saved = anggotaRepository.save(anggota);
        logger.info("SUKSES CREATE: Anggota berhasil disimpan ke Database Write (H2). ID: [{}]", saved.getId());
        
        publishAnggotaCreatedEvent(saved);
        return saved;
    }
    
    @Transactional
    public AnggotaWriteModel handle(UpdateAnggotaCommand command) {
        logger.info("PROSES UPDATE: Memulai update anggota ID: [{}]", command.getId());

        AnggotaWriteModel anggota = anggotaRepository.findById(command.getId())
                .orElseThrow(() -> {
                    logger.error("GAGAL UPDATE: Anggota dengan ID [{}] tidak ditemukan.", command.getId());
                    return new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
                });

        Optional<AnggotaWriteModel> existingNomor = anggotaRepository.findByNomorAnggota(command.getNomorAnggota());
        if (existingNomor.isPresent() && !existingNomor.get().getId().equals(command.getId())) {
            logger.warn("GAGAL UPDATE: Konflik Nomor Anggota [{}]. Sudah digunakan oleh ID lain.", command.getNomorAnggota());
            throw new IllegalArgumentException("Nomor anggota sudah digunakan oleh pengguna lain.");
        }

        Optional<AnggotaWriteModel> existingEmail = anggotaRepository.findByEmail(command.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(command.getId())) {
            logger.warn("GAGAL UPDATE: Konflik Email [{}]. Sudah digunakan oleh ID lain.", command.getEmail());
            throw new IllegalArgumentException("Email sudah digunakan oleh pengguna lain.");
        }

        // Update Data
        anggota.setNomorAnggota(command.getNomorAnggota());
        anggota.setNama(command.getNama());
        anggota.setAlamat(command.getAlamat());
        anggota.setEmail(command.getEmail());
        
        AnggotaWriteModel updated = anggotaRepository.save(anggota);
        logger.info("SUKSES UPDATE: Data anggota ID [{}] berhasil diperbarui di Database Write.", updated.getId());

        publishAnggotaUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public void handle(DeleteAnggotaCommand command) {
        logger.info("PROSES DELETE: Meminta penghapusan anggota ID: [{}]", command.getId());

        if (!anggotaRepository.existsById(command.getId())) {
            logger.warn("GAGAL DELETE: ID [{}] tidak ditemukan.", command.getId());
            throw new IllegalArgumentException("Anggota tidak ditemukan dengan ID: " + command.getId());
        }
        
        anggotaRepository.deleteById(command.getId());
        logger.info("SUKSES DELETE: Anggota ID [{}] telah dihapus dari Database Write.", command.getId());
        
        publishAnggotaDeletedEvent(command.getId());
    }

    private void publishAnggotaCreatedEvent(AnggotaWriteModel anggota) {
        try {
            AnggotaCreatedEvent event = new AnggotaCreatedEvent(
                anggota.getId(),
                anggota.getNomorAnggota(),
                anggota.getNama(),
                anggota.getAlamat(),
                anggota.getEmail()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: AnggotaCreatedEvent dikirim ke RabbitMQ untuk ID [{}]", anggota.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim AnggotaCreatedEvent ke RabbitMQ. Error: {}", e.getMessage());
        }
    }
    
    private void publishAnggotaUpdatedEvent(AnggotaWriteModel anggota) {
        try {
            AnggotaUpdatedEvent event = new AnggotaUpdatedEvent(
                anggota.getId(),
                anggota.getNomorAnggota(),
                anggota.getNama(),
                anggota.getAlamat(),
                anggota.getEmail()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: AnggotaUpdatedEvent dikirim ke RabbitMQ untuk ID [{}]", anggota.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim AnggotaUpdatedEvent. Error: {}", e.getMessage());
        }
    }
    
    private void publishAnggotaDeletedEvent(UUID id) {
        try {
            AnggotaDeletedEvent event = new AnggotaDeletedEvent(id);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: AnggotaDeletedEvent dikirim ke RabbitMQ untuk ID [{}]", id);
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim AnggotaDeletedEvent. Error: {}", e.getMessage());
        }
    }
}
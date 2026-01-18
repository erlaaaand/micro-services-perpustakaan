package com.perpustakaan.service_buku.cqrs.handler;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.entity.command.BukuWriteModel;
import com.perpustakaan.service_buku.event.*;
import com.perpustakaan.service_buku.repository.command.BukuCommandRepository;
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
public class BukuCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(BukuCommandHandler.class);

    private final BukuCommandRepository bukuRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${perpustakaan.rabbitmq.exchange}")
    private String exchange;

    @Value("${perpustakaan.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public BukuWriteModel handle(CreateBukuCommand command) {
        logger.info("PROSES CREATE: Menambahkan buku baru. Kode: [{}], Judul: [{}]", 
            command.getKodeBuku(), command.getJudul());

        // 1. Validasi Duplikat Kode Buku
        if (bukuRepository.existsByKodeBuku(command.getKodeBuku())) {
            logger.warn("GAGAL CREATE: Kode Buku [{}] sudah ada di sistem.", command.getKodeBuku());
            throw new IllegalArgumentException("Kode buku " + command.getKodeBuku() + " sudah digunakan.");
        }

        // 2. Validasi Duplikat Judul Buku
        if (command.getJudul() != null && !command.getJudul().isBlank()) {
            if (bukuRepository.existsByJudul(command.getJudul())) {
                logger.warn("GAGAL CREATE: Judul [{}] sudah terdaftar.", command.getJudul());
                throw new IllegalArgumentException("Judul " + command.getJudul() + " sudah terdaftar.");
            }
        }

        BukuWriteModel buku = new BukuWriteModel();
        buku.setKodeBuku(command.getKodeBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPengarang());
        buku.setPenerbit(command.getPenerbit());
        buku.setTahunTerbit(command.getTahunTerbit());

        BukuWriteModel saved = bukuRepository.save(buku);
        logger.info("SUKSES CREATE: Buku berhasil disimpan (Write DB). ID: [{}]", saved.getId());

        publishBukuCreatedEvent(saved);
        return saved;
    }

    @Transactional
    public BukuWriteModel handle(UpdateBukuCommand command) {
        logger.info("PROSES UPDATE: Memperbarui buku ID: [{}]", command.getId());

        BukuWriteModel buku = bukuRepository.findById(command.getId())
            .orElseThrow(() -> {
                logger.error("GAGAL UPDATE: Buku ID [{}] tidak ditemukan.", command.getId());
                return new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
            });

        // Validasi Konflik Kode Buku (Cek milik orang lain)
        Optional<BukuWriteModel> existingKode = bukuRepository.findByKodeBuku(command.getKodeBuku());
        if (existingKode.isPresent() && !existingKode.get().getId().equals(command.getId())) {
            logger.warn("GAGAL UPDATE: Konflik Kode Buku [{}].", command.getKodeBuku());
            throw new IllegalArgumentException("Kode buku sudah digunakan oleh buku lain.");
        }

        buku.setKodeBuku(command.getKodeBuku());
        buku.setJudul(command.getJudul());
        buku.setPengarang(command.getPengarang());
        buku.setPenerbit(command.getPenerbit());
        buku.setTahunTerbit(command.getTahunTerbit());

        BukuWriteModel updated = bukuRepository.save(buku);
        logger.info("SUKSES UPDATE: Buku ID [{}] berhasil diperbarui.", updated.getId());

        publishBukuUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public void handle(DeleteBukuCommand command) {
        logger.info("PROSES DELETE: Menghapus buku ID: [{}]", command.getId());

        if (!bukuRepository.existsById(command.getId())) {
            logger.warn("GAGAL DELETE: Buku ID [{}] tidak ditemukan.", command.getId());
            throw new IllegalArgumentException("Buku tidak ditemukan dengan ID: " + command.getId());
        }

        bukuRepository.deleteById(command.getId());
        logger.info("SUKSES DELETE: Buku ID [{}] dihapus dari Write DB.", command.getId());

        publishBukuDeletedEvent(command.getId());
    }

    // --- Private Loggers for Events ---

    private void publishBukuCreatedEvent(BukuWriteModel buku) {
        try {
            BukuCreatedEvent event = new BukuCreatedEvent(
                buku.getId(),
                buku.getKodeBuku(),
                buku.getJudul(),
                buku.getPengarang(),
                buku.getPenerbit(),
                buku.getTahunTerbit()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: BukuCreatedEvent dikirim untuk ID [{}]", buku.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim BukuCreatedEvent: {}", e.getMessage());
        }
    }

    private void publishBukuUpdatedEvent(BukuWriteModel buku) {
        try {
            BukuUpdatedEvent event = new BukuUpdatedEvent(
                buku.getId(),
                buku.getKodeBuku(),
                buku.getJudul(),
                buku.getPengarang(),
                buku.getPenerbit(),
                buku.getTahunTerbit()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: BukuUpdatedEvent dikirim untuk ID [{}]", buku.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim BukuUpdatedEvent: {}", e.getMessage());
        }
    }

    private void publishBukuDeletedEvent(UUID id) {
        try {
            BukuDeletedEvent event = new BukuDeletedEvent(id);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: BukuDeletedEvent dikirim untuk ID [{}]", id);
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal mengirim BukuDeletedEvent: {}", e.getMessage());
        }
    }
}
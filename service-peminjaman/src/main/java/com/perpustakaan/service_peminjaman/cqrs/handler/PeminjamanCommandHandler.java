package com.perpustakaan.service_peminjaman.cqrs.handler;

import com.perpustakaan.service_peminjaman.cqrs.command.*;
import com.perpustakaan.service_peminjaman.entity.command.PeminjamanWriteModel;
import com.perpustakaan.service_peminjaman.event.*;
import com.perpustakaan.service_peminjaman.repository.command.PeminjamanRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PeminjamanCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanCommandHandler.class);

    private final PeminjamanRepository peminjamanRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${perpustakaan.rabbitmq.exchange}")
    private String exchange;

    @Value("${perpustakaan.rabbitmq.routing-key}")
    private String routingKey;

    @Transactional
    public PeminjamanWriteModel handle(CreatePeminjamanCommand command) {
        logger.info("PROSES CREATE: Peminjaman baru. Anggota: [{}], Buku: [{}]", 
            command.getAnggotaId(), command.getBukuId());

        // 1. Validasi Logika Bisnis: Tidak boleh meminjam buku yang sama jika status masih DIPINJAM
        if (peminjamanRepository.existsByAnggotaIdAndBukuIdAndStatus(
                command.getAnggotaId(), command.getBukuId(), "DIPINJAM")) {
            logger.warn("GAGAL CREATE: Anggota [{}] sedang meminjam buku [{}] dan belum dikembalikan.", 
                command.getAnggotaId(), command.getBukuId());
            throw new IllegalArgumentException("Anggota ini sedang meminjam buku yang sama.");
        }

        // 2. Validasi Tanggal (Sederhana)
        try {
            LocalDate pinjam = LocalDate.parse(command.getTanggalPinjam());
            LocalDate kembali = LocalDate.parse(command.getTanggalKembali());
            if (kembali.isBefore(pinjam)) {
                logger.warn("GAGAL CREATE: Tanggal kembali [{}] lebih kecil dari tanggal pinjam [{}].", kembali, pinjam);
                throw new IllegalArgumentException("Tanggal kembali tidak boleh sebelum tanggal pinjam.");
            }
        } catch (Exception e) {
            logger.error("GAGAL CREATE: Format tanggal salah.", e);
            throw new IllegalArgumentException("Format tanggal tidak valid.");
        }

        PeminjamanWriteModel peminjaman = new PeminjamanWriteModel();
        peminjaman.setAnggotaId(command.getAnggotaId());
        peminjaman.setBukuId(command.getBukuId());
        peminjaman.setTanggalPinjam(command.getTanggalPinjam());
        peminjaman.setTanggalKembali(command.getTanggalKembali());
        peminjaman.setStatus(command.getStatus());

        PeminjamanWriteModel saved = peminjamanRepository.save(peminjaman);
        logger.info("SUKSES CREATE: Peminjaman disimpan (Write DB). ID Transaksi: [{}]", saved.getId());
        
        publishCreatedEvent(saved);
        return saved;
    }

    @Transactional
    public PeminjamanWriteModel handle(UpdatePeminjamanCommand command) {
        logger.info("PROSES UPDATE: Peminjaman ID: [{}]", command.getId());

        PeminjamanWriteModel peminjaman = peminjamanRepository.findById(command.getId())
            .orElseThrow(() -> {
                logger.error("GAGAL UPDATE: Peminjaman ID [{}] tidak ditemukan.", command.getId());
                return new IllegalArgumentException("Peminjaman not found: " + command.getId());
            });

        // Update data
        peminjaman.setAnggotaId(command.getAnggotaId());
        peminjaman.setBukuId(command.getBukuId());
        peminjaman.setTanggalPinjam(command.getTanggalPinjam());
        peminjaman.setTanggalKembali(command.getTanggalKembali());
        peminjaman.setStatus(command.getStatus());

        PeminjamanWriteModel updated = peminjamanRepository.save(peminjaman);
        logger.info("SUKSES UPDATE: Data peminjaman ID [{}] diperbarui.", updated.getId());
        
        publishUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public PeminjamanWriteModel handleUpdateStatus(UUID id, String status) {
        logger.info("PROSES PATCH: Update Status ID: [{}] -> [{}]", id, status);

        PeminjamanWriteModel peminjaman = peminjamanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Peminjaman not found: " + id));

        peminjaman.setStatus(status);

        PeminjamanWriteModel updated = peminjamanRepository.save(peminjaman);
        logger.info("SUKSES PATCH: Status berubah menjadi [{}]", status);
        
        publishUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public void handle(DeletePeminjamanCommand command) {
        logger.info("PROSES DELETE: Menghapus peminjaman ID: [{}]", command.getId());

        if (!peminjamanRepository.existsById(command.getId())) {
            logger.warn("GAGAL DELETE: ID [{}] tidak ditemukan.", command.getId());
            throw new IllegalArgumentException("Peminjaman not found: " + command.getId());
        }

        peminjamanRepository.deleteById(command.getId());
        logger.info("SUKSES DELETE: Data dihapus dari Write DB.");
        
        publishDeletedEvent(command.getId());
    }

    // --- Private Loggers for Events ---

    private void publishCreatedEvent(PeminjamanWriteModel peminjaman) {
        try {
            PeminjamanCreatedEvent event = new PeminjamanCreatedEvent(
                peminjaman.getId(),
                peminjaman.getAnggotaId(),
                peminjaman.getBukuId(),
                peminjaman.getTanggalPinjam(),
                peminjaman.getStatus()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: PeminjamanCreatedEvent dikirim ID [{}]", peminjaman.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal publish Create Event: {}", e.getMessage());
        }
    }

    private void publishUpdatedEvent(PeminjamanWriteModel peminjaman) {
        try {
            PeminjamanUpdatedEvent event = new PeminjamanUpdatedEvent(
                peminjaman.getId(),
                peminjaman.getStatus(),
                peminjaman.getTanggalKembali()
            );
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: PeminjamanUpdatedEvent dikirim ID [{}]", peminjaman.getId());
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal publish Update Event: {}", e.getMessage());
        }
    }

    private void publishDeletedEvent(UUID id) {
        try {
            PeminjamanDeletedEvent event = new PeminjamanDeletedEvent(id);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("EVENT PUBLISHED: PeminjamanDeletedEvent dikirim ID [{}]", id);
        } catch (Exception e) {
            logger.error("EVENT ERROR: Gagal publish Delete Event: {}", e.getMessage());
        }
    }
}
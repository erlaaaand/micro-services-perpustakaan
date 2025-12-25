package com.perpustakaan.service_pengembalian.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PengembalianEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PengembalianEventListener.class);

    /**
     * Mendengarkan event dari Service Peminjaman.
     * Queue ini harus didefinisikan di RabbitMQConfig dan application.properties
     * (misal: rabbitmq.queue.peminjaman-listener=pengembalian.peminjaman.listener.queue)
     */
    @RabbitListener(queues = "${rabbitmq.queue.peminjaman-listener:pengembalian.peminjaman.listener.queue}")
    public void handlePeminjamanEvent(Object event) {
        // Logika ketika ada transaksi peminjaman baru/update/delete
        // Contoh: Validasi apakah ID Peminjaman valid saat ada pengembalian masuk
        logger.info("Menerima event dari Service Peminjaman: {}", event);
    }

    /**
     * Mendengarkan event dari Service Anggota.
     * Berguna jika Anda menyimpan cache data anggota di database pengembalian.
     */
    @RabbitListener(queues = "${rabbitmq.queue.anggota-listener}")
    public void handleAnggotaEvent(Object event) {
        logger.info("Menerima event dari Service Anggota: {}", event);
    }

    /**
     * Mendengarkan event dari Service Buku.
     */
    @RabbitListener(queues = "${rabbitmq.queue.buku-listener}")
    public void handleBukuEvent(Object event) {
        logger.info("Menerima event dari Service Buku: {}", event);
    }
}
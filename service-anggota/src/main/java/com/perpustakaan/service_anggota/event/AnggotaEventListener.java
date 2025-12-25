package com.perpustakaan.service_anggota.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnggotaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaEventListener.class);

    // Mendengarkan event dari Service Peminjaman
    @RabbitListener(queues = "${rabbitmq.queue.peminjaman-listener}")
    public void handlePeminjamanEvent(Object event) {
        // Disini bisa ditambahkan logika, misal: Update status keaktifan anggota
        // Untuk sekarang kita log saja
        logger.info("Service Anggota menerima notifikasi peminjaman: {}", event);
    }
}
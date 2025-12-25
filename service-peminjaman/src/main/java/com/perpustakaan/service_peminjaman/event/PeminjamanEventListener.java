package com.perpustakaan.service_peminjaman.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PeminjamanEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PeminjamanEventListener.class);

    // Mendengarkan event dari Service Anggota (Queue didefinisikan di RabbitMQConfig)
    @RabbitListener(queues = "${rabbitmq.queue.anggota-listener}")
    public void handleAnggotaEvent(Object event) {
        // Disini logika jika data anggota berubah. 
        // Misalnya: Update cache nama anggota di tabel peminjaman (jika ada denormalisasi)
        logger.info("Menerima update dari Service Anggota: {}", event);
    }

    // Mendengarkan event dari Service Buku
    @RabbitListener(queues = "${rabbitmq.queue.buku-listener}")
    public void handleBukuEvent(Object event) {
        // Disini logika jika data buku berubah
        logger.info("Menerima update dari Service Buku: {}", event);
    }
}
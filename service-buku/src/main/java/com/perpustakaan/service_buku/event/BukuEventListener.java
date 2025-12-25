package com.perpustakaan.service_buku.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BukuEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BukuEventListener.class);

    // Mendengarkan event dari Service Peminjaman
    // Queue ini didefinisikan di application.properties dan RabbitMQConfig
    @RabbitListener(queues = "${rabbitmq.queue.peminjaman-listener}")
    public void handlePeminjamanEvent(Object event) {
        // Disini nanti logika bisnis:
        // Contoh: Jika ada PeminjamanCreatedEvent, kurangi stok buku atau ubah status buku jadi "DIPINJAM"
        logger.info("Service Buku menerima event dari Peminjaman: {}", event);
    }
}
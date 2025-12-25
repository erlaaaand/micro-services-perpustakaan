package com.perpustakaan.service_pengembalian.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Pengembalian Config (Milik Service Ini) ---
    @Value("${rabbitmq.exchange.pengembalian}")
    private String pengembalianExchange;

    @Value("${rabbitmq.queue.pengembalian}")
    private String pengembalianQueue;

    @Value("${rabbitmq.routing-key.pengembalian.created}")
    private String pengembalianCreatedKey;

    @Value("${rabbitmq.routing-key.pengembalian.updated}")
    private String pengembalianUpdatedKey;

    @Value("${rabbitmq.routing-key.pengembalian.deleted}")
    private String pengembalianDeletedKey;

    // --- Listener Config - Peminjaman ---
    @Value("${rabbitmq.exchange.peminjaman}")
    private String peminjamanExchange;
    
    @Value("${rabbitmq.queue.peminjaman-listener}")
    private String peminjamanListenerQueue;

    // --- Listener Config - Anggota ---
    @Value("${rabbitmq.exchange.anggota}")
    private String anggotaExchange;
    
    @Value("${rabbitmq.queue.anggota-listener}")
    private String anggotaListenerQueue;

    // --- Listener Config - Buku ---
    @Value("${rabbitmq.exchange.buku}")
    private String bukuExchange;
    
    @Value("${rabbitmq.queue.buku-listener}")
    private String bukuListenerQueue;

    // ===== Publisher Beans =====
    
    @Bean
    public TopicExchange pengembalianExchange() {
        return new TopicExchange(pengembalianExchange);
    }

    @Bean
    public Queue pengembalianQueue() {
        return new Queue(pengembalianQueue, true);
    }
    
    @Bean
    public Binding pengembalianCreatedBinding() {
        return BindingBuilder.bind(pengembalianQueue())
            .to(pengembalianExchange())
            .with(pengembalianCreatedKey);
    }
    
    @Bean
    public Binding pengembalianUpdatedBinding() {
        return BindingBuilder.bind(pengembalianQueue())
            .to(pengembalianExchange())
            .with(pengembalianUpdatedKey);
    }

    @Bean
    public Binding pengembalianDeletedBinding() {
        return BindingBuilder.bind(pengembalianQueue())
            .to(pengembalianExchange())
            .with(pengembalianDeletedKey);
    }

    // ===== Listener Beans - Peminjaman =====
    
    @Bean
    public TopicExchange peminjamanExchange() {
        return new TopicExchange(peminjamanExchange);
    }

    @Bean
    public Queue peminjamanListenerQueue() {
        return new Queue(peminjamanListenerQueue, true);
    }

    @Bean
    public Binding peminjamanListenerBinding() {
        return BindingBuilder.bind(peminjamanListenerQueue())
            .to(peminjamanExchange())
            .with("peminjaman.#");
    }

    // ===== Listener Beans - Anggota =====
    
    @Bean
    public TopicExchange anggotaExchange() {
        return new TopicExchange(anggotaExchange);
    }

    @Bean
    public Queue anggotaListenerQueue() {
        return new Queue(anggotaListenerQueue, true);
    }

    @Bean
    public Binding anggotaListenerBinding() {
        return BindingBuilder.bind(anggotaListenerQueue())
            .to(anggotaExchange())
            .with("anggota.#");
    }

    // ===== Listener Beans - Buku =====
    
    @Bean
    public TopicExchange bukuExchange() {
        return new TopicExchange(bukuExchange);
    }

    @Bean
    public Queue bukuListenerQueue() {
        return new Queue(bukuListenerQueue, true);
    }

    @Bean
    public Binding bukuListenerBinding() {
        return BindingBuilder.bind(bukuListenerQueue())
            .to(bukuExchange())
            .with("buku.#");
    }

    // ===== Common Beans =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
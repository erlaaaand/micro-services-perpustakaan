package com.perpustakaan.service_buku.config;

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

    // --- Config Publisher (Milik Service Buku) ---
    @Value("${rabbitmq.exchange.buku}")
    private String bukuExchange;
    
    @Value("${rabbitmq.queue.buku}")
    private String bukuQueue;
    
    @Value("${rabbitmq.routing-key.buku.created}")
    private String bukuCreatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.buku.updated}")
    private String bukuUpdatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.buku.deleted}")
    private String bukuDeletedRoutingKey;

    // --- Config Listener (Mendengarkan Service Peminjaman) ---
    @Value("${rabbitmq.exchange.peminjaman}")
    private String peminjamanExchange;
    
    @Value("${rabbitmq.queue.peminjaman-listener}")
    private String peminjamanListenerQueue;

    // --- Bean Definitions ---

    @Bean
    public TopicExchange bukuExchange() {
        return new TopicExchange(bukuExchange);
    }

    @Bean
    public Queue bukuQueue() {
        return new Queue(bukuQueue, true);
    }

    @Bean
    public Binding bukuCreatedBinding() {
        return BindingBuilder.bind(bukuQueue()).to(bukuExchange()).with(bukuCreatedRoutingKey);
    }

    @Bean
    public Binding bukuUpdatedBinding() {
        return BindingBuilder.bind(bukuQueue()).to(bukuExchange()).with(bukuUpdatedRoutingKey);
    }

    @Bean
    public Binding bukuDeletedBinding() {
        return BindingBuilder.bind(bukuQueue()).to(bukuExchange()).with(bukuDeletedRoutingKey);
    }

    // --- Listener Definitions ---
    
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
        return BindingBuilder
            .bind(peminjamanListenerQueue())
            .to(peminjamanExchange())
            .with("peminjaman.#"); // Mendengarkan semua event peminjaman
    }

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
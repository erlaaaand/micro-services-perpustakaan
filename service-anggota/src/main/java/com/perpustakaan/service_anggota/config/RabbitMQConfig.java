package com.perpustakaan.service_anggota.config;

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

    // --- Publisher (Milik Anggota) ---
    @Value("${rabbitmq.exchange.anggota}")
    private String anggotaExchange;
    
    @Value("${rabbitmq.queue.anggota}")
    private String anggotaQueue;
    
    @Value("${rabbitmq.routing-key.anggota.created}")
    private String anggotaCreatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.anggota.updated}")
    private String anggotaUpdatedRoutingKey;
    
    @Value("${rabbitmq.routing-key.anggota.deleted}")
    private String anggotaDeletedRoutingKey;

    // --- Listener (Mendengarkan Peminjaman) ---
    @Value("${rabbitmq.exchange.peminjaman}")
    private String peminjamanExchange;
    
    @Value("${rabbitmq.queue.peminjaman-listener}")
    private String peminjamanListenerQueue;

    // --- Beans Publisher ---
    @Bean
    public TopicExchange anggotaExchange() {
        return new TopicExchange(anggotaExchange);
    }

    @Bean
    public Queue anggotaQueue() {
        return new Queue(anggotaQueue, true);
    }

    @Bean
    public Binding anggotaCreatedBinding() {
        return BindingBuilder.bind(anggotaQueue()).to(anggotaExchange()).with(anggotaCreatedRoutingKey);
    }

    @Bean
    public Binding anggotaUpdatedBinding() {
        return BindingBuilder.bind(anggotaQueue()).to(anggotaExchange()).with(anggotaUpdatedRoutingKey);
    }

    @Bean
    public Binding anggotaDeletedBinding() {
        return BindingBuilder.bind(anggotaQueue()).to(anggotaExchange()).with(anggotaDeletedRoutingKey);
    }

    // --- Beans Listener ---
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
            .with("peminjaman.#");
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
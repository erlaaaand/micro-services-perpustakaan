// service-peminjaman/src/main/java/.../config/RabbitMQConfig.java
package com.perpustakaan.service_peminjaman.config;

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

    @Value("${rabbitmq.exchange.peminjaman}")
    private String peminjamanExchange;
    
    @Value("${rabbitmq.queue.peminjaman}")
    private String peminjamanQueue;
    
    // Subscribe to Anggota events
    @Value("${rabbitmq.exchange.anggota}")
    private String anggotaExchange;
    
    @Value("${rabbitmq.queue.anggota-listener}")
    private String anggotaListenerQueue;
    
    // Subscribe to Buku events
    @Value("${rabbitmq.exchange.buku}")
    private String bukuExchange;
    
    @Value("${rabbitmq.queue.buku-listener}")
    private String bukuListenerQueue;

    @Bean
    public TopicExchange peminjamanExchange() {
        return new TopicExchange(peminjamanExchange);
    }

    @Bean
    public Queue peminjamanQueue() {
        return new Queue(peminjamanQueue, true);
    }

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
        return BindingBuilder
            .bind(anggotaListenerQueue())
            .to(anggotaExchange())
            .with("anggota.#");
    }

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
        return BindingBuilder
            .bind(bukuListenerQueue())
            .to(bukuExchange())
            .with("buku.#");
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
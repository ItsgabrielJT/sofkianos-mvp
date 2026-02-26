package com.sofkianos.producer.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofkianos.producer.domain.model.Kudo;
import com.sofkianos.producer.domain.valueobject.KudoCategory;
import com.sofkianos.producer.infrastructure.messaging.events.KudoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class RabbitMqKudoPublisherTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @Autowired
    private RabbitMqKudoPublisher publisherAdapter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should publish event with correct ISO-8601 date format (Contract Test)")
    void shouldPublishJsonWithIsoDates() {
        // Arrange
        LocalDateTime now = LocalDateTime.of(2026, 2, 11, 12, 0, 0);
        Kudo event = Kudo.create(
                "Alice",
                "Bob",
                KudoCategory.Teamwork,
                "Integration Test");

        // Act
        publisherAdapter.publish(event);

        // Assert: Consume message manually to verify payload contract
        // Since we configured Jackson2JsonMessageConverter, receiveAndConvert should return the object
        Object message = rabbitTemplate.receiveAndConvert("kudos.queue", 5000);
        assertThat(message).isNotNull();
        assertThat(message).isInstanceOf(KudoEvent.class);
        
        KudoEvent receivedEvent = (KudoEvent) message;
        assertThat(receivedEvent.getTimestamp()).isEqualTo(now);
    }
}

package com.sofkianos.producer.infrastructure.messaging;

import com.sofkianos.producer.domain.model.Kudo;
import com.sofkianos.producer.infrastructure.messaging.events.KudoEvent;
import com.sofkianos.producer.domain.ports.out.KudoEventPublisher;
import com.sofkianos.producer.application.exception.KudoMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Adapter that implements the {@link KudoEventPublisher} port.
 * <p>
 * Converts a {@link KudoEvent} to JSON and publishes it to RabbitMQ.
 * All infrastructure concerns (serialization, AMQP transport) are
 * encapsulated here — the domain/service layer never sees them.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqKudoPublisher implements KudoEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    public void publish(Kudo kudo) {
        try {
            KudoEvent event = KudoEvent.builder()
                    .from(kudo.fromUser()).to(kudo.toUser())
                    .category(kudo.category() != null ? kudo.category().name() : null)
                    .message(kudo.message()).timestamp(kudo.createdAt())
                    .build();

            log.info("Publishing KudoEvent to RabbitMQ: from={}, to={}, category={}",
                    event.getFrom(), event.getTo(), event.getCategory());

            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);

            log.debug("KudoEvent published successfully");
        } catch (AmqpException e) {
            log.error("Failed to publish KudoEvent to RabbitMQ", e);
            throw new KudoMessagingException("Error publishing KudoEvent to message broker", e);
        }
    }
}

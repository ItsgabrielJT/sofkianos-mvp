package com.sofkianos.producer.domain.ports.out;


import com.sofkianos.producer.domain.model.Kudo;

/**
 * Output Port — defines the contract for publishing a {@link Kudo}
 * to an external messaging system (e.g., SQS, RabbitMQ, Kafka).
 * <p>
 * The domain layer depends on this <strong>abstraction</strong>;
 * infrastructure adapters provide the concrete implementation.
 * This is the "D" in SOLID (Dependency Inversion Principle).
 * </p>
 */
public interface KudoEventPublisher {
    /**
     * Publishes the given event to the message broker.
     *
     * @param kudo the kudo event to publish — must not be {@code null}
     */
    void publish(Kudo kudo);
}

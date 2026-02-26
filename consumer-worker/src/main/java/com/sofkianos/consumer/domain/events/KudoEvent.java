package com.sofkianos.consumer.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shared DTO representing a Kudo event that travels between
 * the Producer API and the Consumer Worker through the message broker.
 * <p>
 * This is the <strong>contract</strong> that both modules agree upon.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KudoEvent {
    private String from;
    private String to;
    private String category;
    private String message;
    private LocalDateTime timestamp;
}

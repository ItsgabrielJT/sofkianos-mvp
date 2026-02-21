package com.sofkianos.producer.dto;

import java.time.LocalDateTime;

/**
 * Immutable DTO representing a single Kudo item in a paginated list response.
 *
 * @param id        unique identifier
 * @param fromUser  sender (may be masked for privacy)
 * @param toUser    recipient (may be masked for privacy)
 * @param category  kudo category (INNOVATION, TEAMWORK, PASSION, MASTERY)
 * @param message   kudo message body
 * @param createdAt creation timestamp in ISO 8601 format
 */
public record KudoListItemDTO(
        Long id,
        String fromUser,
        String toUser,
        String category,
        String message,
        LocalDateTime createdAt
) {
}

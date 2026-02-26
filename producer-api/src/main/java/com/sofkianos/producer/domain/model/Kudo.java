package com.sofkianos.producer.domain.model;

import com.sofkianos.producer.domain.valueobject.KudoCategory;
import java.time.LocalDateTime;

/**
 * Pure domain model representing a Kudo.
 * This is a 'Record' to ensure immutability and clarity.
 * No JPA or JSON annotations allowed here.
 */
public record Kudo(
        Long id,
        String fromUser,
        String toUser,
        KudoCategory category,
        String message,
        LocalDateTime createdAt) {
    /**
     * Factory method for creating a new Kudo (without ID/Date yet).
     */
    public static Kudo create(String from, String to, KudoCategory category, String message) {
        return new Kudo(null, from, to, category, message, LocalDateTime.now());
    }
}

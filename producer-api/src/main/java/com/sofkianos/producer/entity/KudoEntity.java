package com.sofkianos.producer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only JPA entity mapped to the {@code kudos} table.
 *
 * <p>This entity is used exclusively by the Producer API's query side
 * to read persisted kudos. Write operations are handled by the Consumer Worker.
 * Setters are intentionally omitted to enforce the read-only contract.</p>
 */
@Entity
@Table(name = "kudos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KudoEntity {

    @Id
    private Long id;

    @Column(name = "from_user")
    private String fromUser;

    @Column(name = "to_user")
    private String toUser;

    private String category;

    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

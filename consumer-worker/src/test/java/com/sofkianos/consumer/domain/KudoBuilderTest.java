package com.sofkianos.consumer.domain;

import com.sofkianos.consumer.domain.model.KudoCategory;
import com.sofkianos.consumer.entity.Kudo;
import com.sofkianos.consumer.infrastructure.exception.InvalidKudoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KudoBuilderTest {

    @Test
    @DisplayName("Should build a valid Kudo when all invariants are met")
    void shouldBuildValidKudo() {
        Kudo kudo = Kudo.builder()
                .fromUser("Alice")
                .toUser("Bob")
                .category(KudoCategory.INNOVATION)
                .message("Great idea!")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(kudo).isNotNull();
        assertThat(kudo.getFromUser()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should throw exception when sender equals receiver (Self-Kudo Protection)")
    void shouldThrowException_WhenSenderIsSameAsReceiver() {
        assertThatThrownBy(() -> Kudo.builder()
                .fromUser("Alice")
                .toUser("alice") // Case insensitive check
                .category(KudoCategory.MASTERY)
                .message("I am the best")
                .build())
                .isInstanceOf(InvalidKudoException.class)
                .hasMessageContaining("Cannot send kudo to yourself");
    }

    @Test
    @DisplayName("Should throw exception when message is empty")
    void shouldThrowException_WhenMessageIsEmpty() {
        assertThatThrownBy(() -> Kudo.builder()
                .fromUser("Alice")
                .toUser("Bob")
                .category(KudoCategory.TEAMWORK)
                .message("   ") // Blank message
                .build())
                .isInstanceOf(InvalidKudoException.class)
                .hasMessage("'message' must not be null or empty");
    }
}

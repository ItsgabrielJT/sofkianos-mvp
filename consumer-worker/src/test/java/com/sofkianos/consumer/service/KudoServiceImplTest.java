package com.sofkianos.consumer.service;

import com.sofkianos.consumer.domain.events.KudoEvent;
import com.sofkianos.consumer.domain.model.KudoCategory;
import com.sofkianos.consumer.domain.ports.out.KudoPersistencePort;
import com.sofkianos.consumer.entity.Kudo;
import com.sofkianos.consumer.exception.InvalidKudoException;
import com.sofkianos.consumer.service.impl.KudoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

/**
 * Unit tests for {@link KudoServiceImpl} — Consumer Worker.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>TC-R07-004 — KudoServiceImpl maps KudoEvent to Kudo via validated Builder</li>
 *   <li>TC-R07-006 — Invalid KudoEvent throws InvalidKudoException, persistence NOT invoked</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class KudoServiceImplTest {

    @Mock
    private KudoPersistencePort persistencePort;

    @InjectMocks
    private KudoServiceImpl kudoService;

    @Captor
    private ArgumentCaptor<Kudo> kudoCaptor;

    // ── TC-R07-004 — Valid KudoEvent maps correctly via Builder ─────────

    @Nested
    @DisplayName("TC-R07-004 — Valid KudoEvent → Kudo mapping")
    class ValidMapping {

        @Test
        @DisplayName("Maps all fields correctly from KudoEvent to Kudo entity")
        void saveKudo_mapsAllFieldsCorrectly() {
            LocalDateTime timestamp = LocalDateTime.of(2026, 2, 21, 10, 0);
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Innovation")
                    .message("Great idea on the new architecture!")
                    .timestamp(timestamp)
                    .build();

            kudoService.saveKudo(event);

            verify(persistencePort).save(kudoCaptor.capture());
            Kudo captured = kudoCaptor.getValue();

            assertThat(captured.getFromUser()).isEqualTo("alice@sofka.com");
            assertThat(captured.getToUser()).isEqualTo("bob@sofka.com");
            assertThat(captured.getCategory()).isEqualTo(KudoCategory.INNOVATION);
            assertThat(captured.getMessage()).isEqualTo("Great idea on the new architecture!");
            assertThat(captured.getCreatedAt()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Null timestamp in event causes Builder to set now()")
        void saveKudo_nullTimestamp_builderSetsNow() {
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message("Excellent collaboration!")
                    .timestamp(null)
                    .build();

            kudoService.saveKudo(event);

            verify(persistencePort).save(kudoCaptor.capture());
            Kudo captured = kudoCaptor.getValue();

            assertThat(captured.getCreatedAt())
                    .as("Builder should assign LocalDateTime.now() when timestamp is null")
                    .isNotNull();
        }

        @Test
        @DisplayName("All four categories map correctly")
        void saveKudo_allCategories_mapCorrectly() {
            for (String category : new String[]{"Innovation", "Teamwork", "Passion", "Mastery"}) {
                KudoEvent event = KudoEvent.builder()
                        .from("alice@sofka.com")
                        .to("bob@sofka.com")
                        .category(category)
                        .message("Great work on the project!")
                        .timestamp(LocalDateTime.now())
                        .build();

                kudoService.saveKudo(event);
            }

            verify(persistencePort, org.mockito.Mockito.times(4)).save(any(Kudo.class));
        }
    }

    // ── TC-R07-006 — Invalid KudoEvent → InvalidKudoException ──────────

    @Nested
    @DisplayName("TC-R07-006 — Invalid KudoEvent throws, persistence never invoked")
    class InvalidEvent {

        @Test
        @DisplayName("Empty fromUser throws InvalidKudoException")
        void saveKudo_emptyFrom_throwsException() {
            KudoEvent event = KudoEvent.builder()
                    .from("")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message("Some message here")
                    .build();

            assertThatThrownBy(() -> kudoService.saveKudo(event))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("'fromUser' must not be null or empty");

            verify(persistencePort, never()).save(any());
        }

        @Test
        @DisplayName("Null toUser throws InvalidKudoException")
        void saveKudo_nullTo_throwsException() {
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to(null)
                    .category("Passion")
                    .message("Some inspiring message")
                    .build();

            assertThatThrownBy(() -> kudoService.saveKudo(event))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("'toUser' must not be null or empty");

            verify(persistencePort, never()).save(any());
        }

        @Test
        @DisplayName("Self-kudo (from == to) throws InvalidKudoException")
        void saveKudo_selfKudo_throwsException() {
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("alice@sofka.com")
                    .category("Mastery")
                    .message("I am great at everything!")
                    .build();

            assertThatThrownBy(() -> kudoService.saveKudo(event))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Cannot send kudo to yourself");

            verify(persistencePort, never()).save(any());
        }

        @Test
        @DisplayName("Unknown category throws IllegalArgumentException")
        void saveKudo_unknownCategory_throwsException() {
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Leadership")
                    .message("Great leadership skills!")
                    .build();

            assertThatThrownBy(() -> kudoService.saveKudo(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown KudoCategory");

            verify(persistencePort, never()).save(any());
        }

        @Test
        @DisplayName("Empty message throws InvalidKudoException")
        void saveKudo_emptyMessage_throwsException() {
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Innovation")
                    .message("   ")
                    .build();

            assertThatThrownBy(() -> kudoService.saveKudo(event))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("'message' must not be null or empty");

            verify(persistencePort, never()).save(any());
        }
    }
}

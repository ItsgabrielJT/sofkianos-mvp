package com.sofkianos.consumer.component;

import com.sofkianos.consumer.domain.events.KudoEvent;
import com.sofkianos.consumer.service.KudoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.same;

/**
 * Unit tests for {@link KudosConsumer}.
 *
 * <p>Covers TC-R07-001: KudosConsumer receives typed KudoEvent (not String)
 * and delegates directly to KudoService without manual parsing.</p>
 */
@ExtendWith(MockitoExtension.class)
class KudosConsumerTest {

    @Mock
    private KudoService kudoService;

    @InjectMocks
    private KudosConsumer consumer;

    @Test
    @DisplayName("TC-R07-001 — handleKudo receives typed KudoEvent and delegates to service")
    void handleKudo_receivesTypedKudoEvent_delegatesToService() {
        KudoEvent event = KudoEvent.builder()
                .from("alice@sofka.com")
                .to("bob@sofka.com")
                .category("Teamwork")
                .message("Great collaboration on the sprint!")
                .timestamp(LocalDateTime.of(2026, 2, 21, 10, 0))
                .build();

        assertDoesNotThrow(() -> consumer.handleKudo(event));
        verify(kudoService, times(1)).saveKudo(same(event));
    }

    @Test
    @DisplayName("TC-R07-001 — handleKudo with minimal KudoEvent (null timestamp) delegates correctly")
    void handleKudo_minimalEvent_delegatesToService() {
        KudoEvent event = KudoEvent.builder()
                .from("alice@sofka.com")
                .to("bob@sofka.com")
                .category("Passion")
                .message("Keep up the energy!")
                .build();

        assertDoesNotThrow(() -> consumer.handleKudo(event));
        verify(kudoService, times(1)).saveKudo(same(event));
    }
}
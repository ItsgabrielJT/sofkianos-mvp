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

  @Test
  @DisplayName("receiveKudo processes message and completes without exception")
  void receiveKudo_processesMessageWithoutException() {
    KudosConsumer consumer = new KudosConsumer(kudoService);
    KudoEvent event = KudoEvent.builder()
        .from("alice@sofkianos.com")
        .to("bob@sofkianos.com")
        .category("TEAMWORK")
        .message("Great job on the sprint delivery!")
        .build();

    assertDoesNotThrow(() -> consumer.handleKudo(event));
    verify(kudoService, times(1)).saveKudo(event);
  }
}
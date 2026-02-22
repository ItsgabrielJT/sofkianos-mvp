package com.sofkianos.producer.service.impl;

import com.sofkianos.producer.domain.events.KudoEvent;
import com.sofkianos.producer.domain.ports.out.KudoEventPublisher;
import com.sofkianos.producer.domain.validation.KudoValidationContext;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.dto.KudoResponse;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link KudoServiceImpl} — Producer API.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>TC-R12-003 — validate() executes BEFORE publish()</li>
 *   <li>TC-R12-004 — Failed validation prevents publishing</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class KudoServiceImplTest {

    @Mock
    private KudoValidationContext validationContext;

    @Mock
    private KudoEventPublisher kudoEventPublisher;

    @InjectMocks
    private KudoServiceImpl kudoService;

    // ── TC-R12-003 — Validate before publish ────────────────────────────

    @Nested
    @DisplayName("TC-R12-003 — Validation executes BEFORE publishing")
    class ValidateBeforePublish {

        @Test
        @DisplayName("validate() is called before publish() in strict order")
        void sendKudo_validateBeforePublish() {
            KudoRequest request = validRequest();
            doNothing().when(validationContext).validate(any());

            kudoService.sendKudo(request);

            InOrder order = inOrder(validationContext, kudoEventPublisher);
            order.verify(validationContext).validate(request);
            order.verify(kudoEventPublisher).publish(any(KudoEvent.class));
        }

        @Test
        @DisplayName("Valid request returns KudoResponse with ACCEPTED status")
        void sendKudo_validRequest_returnsAccepted() {
            KudoRequest request = validRequest();
            doNothing().when(validationContext).validate(any());

            KudoResponse response = kudoService.sendKudo(request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("ACCEPTED");
            assertThat(response.getId()).isNotBlank();
            assertThat(response.getMessage()).isEqualTo("Kudo queued successfully");
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Both validation and publishing are invoked exactly once")
        void sendKudo_bothInvokedOnce() {
            KudoRequest request = validRequest();
            doNothing().when(validationContext).validate(any());

            kudoService.sendKudo(request);

            verify(validationContext).validate(request);
            verify(kudoEventPublisher).publish(any(KudoEvent.class));
        }
    }

    // ── TC-R12-004 — Failed validation blocks publishing ────────────────

    @Nested
    @DisplayName("TC-R12-004 — Validation failure prevents publishing")
    class ValidationBlocksPublishing {

        @Test
        @DisplayName("InvalidKudoException from validation → publish() NEVER called")
        void sendKudo_validationFails_publishNeverCalled() {
            KudoRequest request = validRequest();
            doThrow(new InvalidKudoException("Cannot send a kudo to yourself"))
                    .when(validationContext).validate(request);

            assertThatThrownBy(() -> kudoService.sendKudo(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessage("Cannot send a kudo to yourself");

            verify(kudoEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Unsupported category → publish() NEVER called")
        void sendKudo_unsupportedCategory_publishNeverCalled() {
            KudoRequest request = KudoRequest.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Leadership")
                    .message("Great leadership skills demonstrated in the last sprint!")
                    .build();

            doThrow(new InvalidKudoException("Unsupported category: Leadership"))
                    .when(validationContext).validate(request);

            assertThatThrownBy(() -> kudoService.sendKudo(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Unsupported category");

            verify(kudoEventPublisher, never()).publish(any());
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private KudoRequest validRequest() {
        return KudoRequest.builder()
                .from("alice@sofka.com")
                .to("bob@sofka.com")
                .category("Teamwork")
                .message("Excellent collaboration on the cloud migration project!")
                .build();
    }
}

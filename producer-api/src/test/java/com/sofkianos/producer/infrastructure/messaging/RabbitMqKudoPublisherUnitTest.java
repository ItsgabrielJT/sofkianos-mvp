package com.sofkianos.producer.infrastructure.messaging;

import com.sofkianos.producer.domain.events.KudoEvent;
import com.sofkianos.producer.exception.KudoPublishingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RabbitMqKudoPublisher} — TC-R05-001 / TC-R05-002.
 *
 * <p>Pure Mockito tests — no Spring context loaded.
 * Validates the Adapter contract: delegation to {@code RabbitTemplate}
 * and exception wrapping into {@link KudoPublishingException}.</p>
 *
 * <p><b>Principle</b>: "Las pruebas dependen del contexto" — the adapter
 * is the boundary where serialization failures and broker connectivity
 * issues manifest, making it a critical test surface for the "Kudo Fantasma" risk.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US-005: RabbitMqKudoPublisher — Unit Tests (Mockito)")
class RabbitMqKudoPublisherUnitTest {

    private static final String TEST_EXCHANGE = "kudos.exchange";
    private static final String TEST_ROUTING_KEY = "kudos.key";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<KudoEvent> eventCaptor;

    private RabbitMqKudoPublisher publisher;

    @BeforeEach
    void setUp() throws Exception {
        publisher = new RabbitMqKudoPublisher(rabbitTemplate);
        injectFieldValue(publisher, "exchangeName", TEST_EXCHANGE);
        injectFieldValue(publisher, "routingKey", TEST_ROUTING_KEY);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TC-R05-001: Adapter delega correctamente a RabbitTemplate
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-R05-001: Adapter delega correctamente a RabbitTemplate")
    class DelegationToRabbitTemplate {

        /**
         * Given un KudoEvent válido con todos los campos
         * When se invoca publish(event)
         * Then rabbitTemplate.convertAndSend es invocado 1 vez con
         *      exchange="kudos.exchange", routingKey="kudos.key", y el evento exacto
         */
        @Test
        @DisplayName("publish delega a convertAndSend con exchange, routingKey y evento correctos")
        void publish_delegatesToConvertAndSend_withCorrectArguments() {
            // Arrange
            KudoEvent event = buildValidEvent();
            doNothing().when(rabbitTemplate)
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));

            // Act
            publisher.publish(event);

            // Assert
            verify(rabbitTemplate, times(1))
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), eventCaptor.capture());

            KudoEvent captured = eventCaptor.getValue();
            assertThat(captured).isSameAs(event);
        }

        /**
         * Given un KudoEvent con campos mínimos (sin message ni timestamp)
         * When se invoca publish(event)
         * Then el adapter delega sin validar — la validación es responsabilidad del dominio
         */
        @Test
        @DisplayName("publish delega evento con campos mínimos sin validar contenido")
        void publish_delegatesMinimalEvent_withoutValidation() {
            // Arrange
            KudoEvent minimalEvent = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .build();

            // Act
            publisher.publish(minimalEvent);

            // Assert
            verify(rabbitTemplate, times(1))
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), eq(minimalEvent));
        }

        /**
         * Given KudoEvents con las 4 categorías válidas
         * When se invoca publish(event) para cada una
         * Then todas se delegan correctamente sin discriminación por categoría
         */
        @Test
        @DisplayName("publish acepta cualquier categoría — Innovation, Teamwork, Passion, Mastery")
        void publish_acceptsAllCategories() {
            // Arrange
            String[] categories = {"Innovation", "Teamwork", "Passion", "Mastery"};

            for (String category : categories) {
                KudoEvent event = KudoEvent.builder()
                        .from("alice@sofka.com")
                        .to("bob@sofka.com")
                        .category(category)
                        .message("Test message for " + category)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Act
                publisher.publish(event);
            }

            // Assert — 4 invocaciones, una por categoría
            verify(rabbitTemplate, times(4))
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));
        }

        /**
         * Given un KudoEvent con mensaje de 500 caracteres (límite máximo)
         * When se invoca publish(event)
         * Then el adapter delega correctamente sin truncar
         */
        @Test
        @DisplayName("publish delega evento con mensaje en límite máximo (500 chars)")
        void publish_delegatesEventWithMaxLengthMessage() {
            // Arrange
            String longMessage = "a".repeat(500);
            KudoEvent event = KudoEvent.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message(longMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Act
            publisher.publish(event);

            // Assert
            verify(rabbitTemplate, times(1))
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), eventCaptor.capture());

            assertThat(eventCaptor.getValue().getMessage()).hasSize(500);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TC-R05-002: AmqpException envuelta en KudoPublishingException
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-R05-002: AmqpException se envuelve en KudoPublishingException")
    class ExceptionWrapping {

        /**
         * Given rabbitTemplate.convertAndSend lanza AmqpException("Connection refused")
         * When se invoca publish(event)
         * Then se lanza KudoPublishingException con mensaje descriptivo
         * And la causa original (AmqpException) se preserva en getCause()
         */
        @Test
        @DisplayName("AmqpException genérica se envuelve en KudoPublishingException con causa preservada")
        void publish_wrapsAmqpException_inKudoPublishingException() {
            // Arrange
            KudoEvent event = buildValidEvent();
            AmqpException amqpException = new AmqpException("Connection refused");
            doThrow(amqpException)
                    .when(rabbitTemplate)
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));

            // Act & Assert
            assertThatThrownBy(() -> publisher.publish(event))
                    .isInstanceOf(KudoPublishingException.class)
                    .hasMessageContaining("Error publishing KudoEvent to message broker")
                    .hasCause(amqpException);
        }

        /**
         * Given rabbitTemplate.convertAndSend lanza AmqpConnectException (conexión rechazada)
         * When se invoca publish(event)
         * Then se captura como AmqpException (polimorfismo) y se envuelve
         */
        @Test
        @DisplayName("AmqpConnectException se envuelve en KudoPublishingException")
        void publish_wrapsAmqpConnectException() {
            // Arrange
            KudoEvent event = buildValidEvent();
            AmqpConnectException connectException = new AmqpConnectException(
                    new java.net.ConnectException("Connection refused: localhost:5672"));
            doThrow(connectException)
                    .when(rabbitTemplate)
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));

            // Act & Assert
            assertThatThrownBy(() -> publisher.publish(event))
                    .isInstanceOf(KudoPublishingException.class)
                    .hasCauseInstanceOf(AmqpConnectException.class);
        }

        /**
         * Given rabbitTemplate.convertAndSend lanza AmqpIOException (I/O failure)
         * When se invoca publish(event)
         * Then se envuelve en KudoPublishingException preservando la causa
         */
        @Test
        @DisplayName("AmqpIOException se envuelve en KudoPublishingException")
        void publish_wrapsAmqpIOException() {
            // Arrange
            KudoEvent event = buildValidEvent();
            AmqpIOException ioException = new AmqpIOException(
                    new java.io.IOException("Broker I/O failure"));
            doThrow(ioException)
                    .when(rabbitTemplate)
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));

            // Act & Assert
            assertThatThrownBy(() -> publisher.publish(event))
                    .isInstanceOf(KudoPublishingException.class)
                    .hasCauseInstanceOf(AmqpIOException.class);
        }

        /**
         * Given rabbitTemplate.convertAndSend NO lanza excepción
         * When se invoca publish(event)
         * Then NO se lanza ninguna excepción
         */
        @Test
        @DisplayName("Publicación exitosa no lanza excepción")
        void publish_doesNotThrow_whenRabbitTemplateSucceeds() {
            // Arrange
            KudoEvent event = buildValidEvent();

            // Act & Assert — no exception expected
            publisher.publish(event);

            verify(rabbitTemplate, times(1))
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), eq(event));
        }

        /**
         * Given rabbitTemplate.convertAndSend lanza AmqpException con mensaje null
         * When se invoca publish(event)
         * Then KudoPublishingException tiene su propio mensaje fijo
         */
        @Test
        @DisplayName("AmqpException con mensaje null genera KudoPublishingException con mensaje propio")
        void publish_wrapsAmqpExceptionWithNullMessage() {
            // Arrange
            KudoEvent event = buildValidEvent();
            AmqpException exWithNullMsg = new AmqpException((String) null);
            doThrow(exWithNullMsg)
                    .when(rabbitTemplate)
                    .convertAndSend(eq(TEST_EXCHANGE), eq(TEST_ROUTING_KEY), any(KudoEvent.class));

            // Act & Assert
            assertThatThrownBy(() -> publisher.publish(event))
                    .isInstanceOf(KudoPublishingException.class)
                    .hasMessageContaining("Error publishing KudoEvent to message broker")
                    .hasCause(exWithNullMsg);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private static KudoEvent buildValidEvent() {
        return KudoEvent.builder()
                .from("alice@sofka.com")
                .to("bob@sofka.com")
                .category("Teamwork")
                .message("Great collaboration on the project!")
                .timestamp(LocalDateTime.of(2026, 2, 21, 10, 0, 0))
                .build();
    }

    /**
     * Injects a value into a private field annotated with @Value,
     * since Mockito does not handle @Value injection.
     */
    private static void injectFieldValue(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

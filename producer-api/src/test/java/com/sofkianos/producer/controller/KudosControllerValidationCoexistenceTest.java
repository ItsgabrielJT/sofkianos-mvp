package com.sofkianos.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.dto.KudoResponse;
import com.sofkianos.producer.exception.InvalidKudoException;
import com.sofkianos.producer.infrastructure.controller.advice.GlobalExceptionHandler;
import com.sofkianos.producer.service.KudoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level tests for coexistence of Bean Validation (API layer)
 * and Strategy Validation (domain layer).
 *
 * <p>Covers TC-R12-006.</p>
 */
@WebMvcTest(KudosController.class)
@Import(GlobalExceptionHandler.class)
class KudosControllerValidationCoexistenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KudoService kudoService;

    // ── Bean Validation rejects BEFORE reaching service ─────────────────

    @Nested
    @DisplayName("TC-R12-006 — Bean Validation layer (API)")
    class BeanValidationLayer {

        @Test
        @DisplayName("Empty 'from' fails Bean Validation → 400, service never called")
        void publishKudos_emptyFrom_returns400() throws Exception {
            KudoRequest request = KudoRequest.builder()
                    .from("")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message("A valid message that is long enough")
                    .build();

            mockMvc.perform(post("/api/v1/kudos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation failed"));

            verify(kudoService, never()).sendKudo(any());
        }

        @Test
        @DisplayName("Invalid email fails Bean Validation → 400, service never called")
        void publishKudos_invalidEmail_returns400() throws Exception {
            KudoRequest request = KudoRequest.builder()
                    .from("not-an-email")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message("A valid message that is long enough")
                    .build();

            mockMvc.perform(post("/api/v1/kudos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").isNotEmpty());

            verify(kudoService, never()).sendKudo(any());
        }

        @Test
        @DisplayName("Invalid category pattern fails Bean Validation → 400")
        void publishKudos_invalidCategory_returns400() throws Exception {
            KudoRequest request = KudoRequest.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("INNOVATION")
                    .message("A valid message that is long enough")
                    .build();

            mockMvc.perform(post("/api/v1/kudos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(kudoService, never()).sendKudo(any());
        }
    }

    // ── Strategy Validation rejects after Bean Validation passes ────────

    @Nested
    @DisplayName("TC-R12-006 — Strategy Validation layer (Domain)")
    class StrategyValidationLayer {

        @Test
        @DisplayName("Self-kudo: Bean Validation passes, Strategy rejects → 400")
        void publishKudos_selfKudo_returns400() throws Exception {
            KudoRequest request = KudoRequest.builder()
                    .from("alice@sofka.com")
                    .to("alice@sofka.com")
                    .category("Teamwork")
                    .message("This is a valid format message to bypass Bean Validation")
                    .build();

            when(kudoService.sendKudo(any()))
                    .thenThrow(new InvalidKudoException("Cannot send a kudo to yourself"));

            mockMvc.perform(post("/api/v1/kudos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Domain validation failed"))
                    .andExpect(jsonPath("$.detail").value("Cannot send a kudo to yourself"));
        }
    }

    // ── Happy path: both layers pass → 202 ─────────────────────────────

    @Nested
    @DisplayName("TC-R12-006 — Both validation layers pass")
    class HappyPath {

        @Test
        @DisplayName("Valid request → Bean Validation passes → Strategy passes → 202")
        void publishKudos_validRequest_returns202() throws Exception {
            KudoRequest request = KudoRequest.builder()
                    .from("alice@sofka.com")
                    .to("bob@sofka.com")
                    .category("Teamwork")
                    .message("Excellent collaboration on the cloud migration project!")
                    .build();

            when(kudoService.sendKudo(any())).thenReturn(
                    KudoResponse.builder()
                            .id("abc-123")
                            .status("ACCEPTED")
                            .message("Kudo queued successfully")
                            .timestamp(LocalDateTime.now())
                            .build());

            mockMvc.perform(post("/api/v1/kudos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.id").value("abc-123"));
        }
    }
}

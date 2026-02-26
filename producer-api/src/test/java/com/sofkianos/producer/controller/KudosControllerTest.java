package com.sofkianos.producer.controller;

import com.sofkianos.producer.application.dto.KudoRequest;
import com.sofkianos.producer.application.dto.KudoResponse;
import com.sofkianos.producer.application.ports.in.KudoService;
import com.sofkianos.producer.infrastructure.inbound.web.KudosController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KudosControllerTest {

    @Mock
    private KudoService kudoService;

    private KudosController kudosController;

    @BeforeEach
    void setUp() {
        kudosController = new KudosController(kudoService);
    }

    @Test
    void publishKudos_WithValidPayload_ReturnsAccepted() {
        // Arrange
        KudoRequest request = KudoRequest.builder()
                .from("user@example.com")
                .to("peer@example.com")
                .category("Teamwork")
                .message("Great job on the project!")
                .build();
        
        KudoResponse mockResponse = KudoResponse.builder()
                .id("123-abc")
                .status("ACCEPTED")
                .build();

        when(kudoService.sendKudo(any(KudoRequest.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<KudoResponse> response = kudosController.publishKudos(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(kudoService, times(1)).sendKudo(any(KudoRequest.class));
        assertEquals("123-abc", response.getBody().getId());
    }
}
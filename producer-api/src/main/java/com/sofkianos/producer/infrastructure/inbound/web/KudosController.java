package com.sofkianos.producer.infrastructure.inbound.web;

import com.sofkianos.producer.application.dto.KudoRequest;
import com.sofkianos.producer.application.dto.KudoResponse;
import com.sofkianos.producer.application.ports.in.KudoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that accepts Kudos and delegates to the service layer.
 */
@RestController
@RequestMapping("/api/v1/kudos")
@RequiredArgsConstructor
public class KudosController {
  private final KudoService kudoService;

  @PostMapping
  public ResponseEntity<KudoResponse> publishKudos(@Valid @RequestBody KudoRequest payload) {
    var response = kudoService.sendKudo(payload);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
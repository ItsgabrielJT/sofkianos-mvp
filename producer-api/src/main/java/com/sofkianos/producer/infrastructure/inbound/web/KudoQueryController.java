package com.sofkianos.producer.controller;

import com.sofkianos.producer.dto.KudoListRequest;
import com.sofkianos.producer.dto.PagedKudoResponse;
import com.sofkianos.producer.service.KudoQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the public Kudo listing endpoint (query side).
 *
 * <p>Separated from {@link KudosController} (command side) following
 * CQRS principles. This controller only handles read operations.</p>
 *
 * <p>Validation is handled via {@link KudoListRequest} which defines
 * constraints for pagination and sorting parameters.</p>
 */
@RestController
@RequestMapping("/api/v1/kudos")
@RequiredArgsConstructor
public class KudoQueryController {

    private final KudoQueryService kudoQueryService;

    @GetMapping
    public ResponseEntity<PagedKudoResponse> listKudos(@Valid KudoListRequest request) {
        PagedKudoResponse response = kudoQueryService.listKudos(
                request.getPage(), request.getSize(), request.getSortDirection()
        );
        return ResponseEntity.ok(response);
    }
}

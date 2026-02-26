package com.sofkianos.consumer.controller;

import com.sofkianos.consumer.service.KudoService;
import com.sofkianos.consumer.dto.PagedKudoResponse;
import com.sofkianos.consumer.dto.KudoSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kudos")
@RequiredArgsConstructor
public class KudosController {
    private final KudoService kudoService;

    @Operation(summary = "Get paginated Kudos", description = "Returns a paginated list of Kudos")
    @GetMapping
    public ResponseEntity<PagedKudoResponse> getKudos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchText) {

        KudoSearchCriteria criteria = KudoSearchCriteria.builder()
                .page(page)
                .size(size)
                .sortDirection(sortDirection)
                .category(category)
                .searchText(searchText)
                .build();

        var result = kudoService.searchKudos(criteria);
        return ResponseEntity.ok(result);
    }
}

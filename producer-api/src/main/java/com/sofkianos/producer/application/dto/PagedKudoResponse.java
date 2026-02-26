package com.sofkianos.producer.dto;

import java.util.List;

/**
 * Immutable DTO representing a paginated response of Kudo items.
 *
 * @param content       list of kudo items for the current page
 * @param totalElements total number of kudos across all pages
 * @param totalPages    total number of pages available
 * @param currentPage   zero-based index of the current page
 * @param size          maximum number of items per page
 */
public record PagedKudoResponse(
        List<KudoListItemDTO> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {
}

package com.sofkianos.producer.service;

import com.sofkianos.producer.dto.PagedKudoResponse;

/**
 * Query service for reading Kudos from the persistence layer.
 *
 * <p>Separated from {@link KudoService} (command side) following
 * CQRS principles — reads and writes use distinct interfaces.</p>
 */
public interface KudoQueryService {

    /**
     * Lists kudos with pagination and sorting.
     *
     * @param page          zero-based page index
     * @param size          number of items per page (1–50)
     * @param sortDirection sort direction for createdAt field (ASC or DESC)
     * @return paginated response with kudo items and metadata
     */
    PagedKudoResponse listKudos(int page, int size, String sortDirection);
}

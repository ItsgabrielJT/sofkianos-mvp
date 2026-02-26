package com.sofkianos.consumer.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import org.springframework.data.domain.Page;

@Data
@Builder
public class PagedKudoResponse {
    private List<KudoResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static PagedKudoResponse from(Page<com.sofkianos.consumer.entity.Kudo> page) {
        List<KudoResponse> content = page.getContent().stream()
                .map(KudoResponse::fromEntity)
                .toList();
        return PagedKudoResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}

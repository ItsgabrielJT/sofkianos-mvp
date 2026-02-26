package com.sofkianos.consumer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KudoSearchCriteria {
    private int page;
    private int size;
    private String sortDirection;
    private String category;
    private String searchText;
}

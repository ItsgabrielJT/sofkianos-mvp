package com.sofkianos.producer.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KudoResponse {
    private String id;
    private String message;
    private String status;
    private LocalDateTime timestamp;
}

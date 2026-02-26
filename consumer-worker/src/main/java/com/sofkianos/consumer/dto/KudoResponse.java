package com.sofkianos.consumer.dto;

import com.sofkianos.consumer.entity.Kudo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KudoResponse {
    private Long id;
    private String fromUser;
    private String toUser;
    private String category;
    private String message;
    private java.time.LocalDateTime createdAt;

    public static KudoResponse fromEntity(Kudo kudo) {
        return KudoResponse.builder()
                .id(kudo.getId())
                .fromUser(kudo.getFromUser())
                .toUser(kudo.getToUser())
                .category(kudo.getCategory() != null ? kudo.getCategory().name() : null)
                .message(kudo.getMessage())
                .createdAt(kudo.getCreatedAt())
                .build();
    }
}

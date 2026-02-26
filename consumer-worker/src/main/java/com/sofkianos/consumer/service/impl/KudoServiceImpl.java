package com.sofkianos.consumer.service.impl;

import com.sofkianos.consumer.domain.events.KudoEvent;
import com.sofkianos.consumer.domain.ports.out.KudoPersistencePort;
import com.sofkianos.consumer.entity.Kudo;
import com.sofkianos.consumer.infrastructure.exception.InvalidKudoException;
import com.sofkianos.consumer.service.KudoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class KudoServiceImpl implements KudoService {
    private final KudoPersistencePort persistencePort;

    @Override
    public void saveKudo(KudoEvent event) {
        Kudo kudo = Optional.ofNullable(event)
                .filter(e -> e.getFrom() != null && !e.getFrom().isBlank())
                .filter(e -> e.getTo() != null && !e.getTo().isBlank())
                .filter(e -> e.getMessage() != null && !e.getMessage().isBlank())
                .map(e -> {
                    log.info("Mapping KudoEvent to entity: from={}, to={}", e.getFrom(), e.getTo());
                    return Kudo.builder()
                            .fromUser(e.getFrom())
                            .toUser(e.getTo())
                            .category(e.getCategory())
                            .message(e.getMessage())
                            .createdAt(e.getTimestamp())
                            .build();
                }).orElseThrow(() -> new InvalidKudoException(
                        "KudoEvent is missing required fields: from, to or message"));

        persistencePort.save(kudo);
        log.info("Kudo persisted successfully: from={}, to={}, category={}",
                kudo.getFromUser(), kudo.getToUser(), kudo.getCategory());
    }

    @Override
    public com.sofkianos.consumer.dto.PagedKudoResponse searchKudos(com.sofkianos.consumer.dto.KudoSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.getPage(),
                criteria.getSize()
        );
        Page<com.sofkianos.consumer.entity.Kudo> page = persistencePort.findKudos(
                Optional.ofNullable(criteria.getCategory()),
                Optional.ofNullable(criteria.getSearchText()),
                pageable
        );
        return com.sofkianos.consumer.dto.PagedKudoResponse.from(page);
    }
}
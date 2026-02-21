package com.sofkianos.producer.service.impl;

import com.sofkianos.producer.dto.KudoListItemDTO;
import com.sofkianos.producer.dto.PagedKudoResponse;
import com.sofkianos.producer.entity.KudoEntity;
import com.sofkianos.producer.repository.KudoReadRepository;
import com.sofkianos.producer.service.KudoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link KudoQueryService} that reads kudos
 * from PostgreSQL via {@link KudoReadRepository}.
 *
 * <p>All operations are marked {@code readOnly = true} to enforce
 * the read-only contract of the Producer API's query side.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KudoQueryServiceImpl implements KudoQueryService {

    private static final String SORT_FIELD = "createdAt";

    private final KudoReadRepository kudoReadRepository;

    @Override
    public PagedKudoResponse listKudos(int page, int size, String sortDirection) {
        Sort.Direction direction = Sort.Direction.valueOf(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, SORT_FIELD));

        Page<KudoEntity> kudoPage = kudoReadRepository.findAll(pageable);

        return toPagedResponse(kudoPage);
    }

    private PagedKudoResponse toPagedResponse(Page<KudoEntity> kudoPage) {
        List<KudoListItemDTO> content = kudoPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PagedKudoResponse(
                content,
                kudoPage.getTotalElements(),
                kudoPage.getTotalPages(),
                kudoPage.getNumber(),
                kudoPage.getSize()
        );
    }

    private KudoListItemDTO toDTO(KudoEntity entity) {
        return new KudoListItemDTO(
                entity.getId(),
                entity.getFromUser(),
                entity.getToUser(),
                entity.getCategory(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}

package com.sofkianos.consumer.infrastructure.persistence;

import com.sofkianos.consumer.domain.ports.out.KudoPersistencePort;
import com.sofkianos.consumer.entity.Kudo;
import com.sofkianos.consumer.repository.KudoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaKudoPersistenceAdapter implements KudoPersistencePort {

    private final KudoRepository kudoRepository;

    @Override
    public Kudo save(Kudo kudo) {
        log.debug("Persisting Kudo via JPA: from={}, to={}",
                kudo.getFromUser(), kudo.getToUser());

        Kudo saved = kudoRepository.save(kudo);

        log.debug("Kudo persisted with id={}", saved.getId());
        return saved;
    }

    @Override
    public org.springframework.data.domain.Page<Kudo> findKudos(java.util.Optional<String> category, java.util.Optional<String> searchText, org.springframework.data.domain.Pageable pageable) {
        return kudoRepository.findKudos(category.orElse(null), searchText.orElse(null), pageable);
    }
}

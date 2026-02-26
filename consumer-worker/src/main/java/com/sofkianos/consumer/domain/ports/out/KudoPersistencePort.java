package com.sofkianos.consumer.domain.ports.out;

import com.sofkianos.consumer.entity.Kudo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KudoPersistencePort {

    Kudo save(Kudo kudo);

    Page<Kudo> findKudos(Optional<String> category, Optional<String> searchText, Pageable pageable);
}

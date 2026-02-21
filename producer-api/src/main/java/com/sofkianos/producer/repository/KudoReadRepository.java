package com.sofkianos.producer.repository;

import com.sofkianos.producer.entity.KudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Read-only repository for querying persisted Kudos.
 *
 * <p>Extends {@link JpaRepository} to leverage built-in
 * {@code findAll(Pageable)} for paginated queries.</p>
 */
@Repository
public interface KudoReadRepository extends JpaRepository<KudoEntity, Long> {
}

package com.sofkianos.producer.domain.ports.out;

import com.sofkianos.producer.domain.model.Kudo;

import java.util.Optional;

/**
 * Puerto de Salida para persistencia de Kudos.
 * Desacopla el dominio de Spring Data JPA.
 */
public interface KudoRepository {
    Kudo save(Kudo kudo);
    Optional<Kudo> findById(Long id);
}

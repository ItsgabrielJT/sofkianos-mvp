package com.sofkianos.producer.application.ports.in;

import com.sofkianos.producer.application.dto.KudoRequest;
import com.sofkianos.producer.application.dto.KudoResponse;

/**
 * Service interface for handling Kudos.
 */
public interface KudoService {
  /**
   * Processes a Kudo request and publishes it to the messaging system.
   *
   * @param kudoRequest the kudo to process
   * @return the response containing tracking details
   */
  KudoResponse sendKudo(KudoRequest kudoRequest);
}
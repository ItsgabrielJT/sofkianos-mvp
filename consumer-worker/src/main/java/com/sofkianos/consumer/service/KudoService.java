package com.sofkianos.consumer.service;

import com.sofkianos.consumer.domain.events.KudoEvent;
import com.sofkianos.consumer.dto.KudoSearchCriteria;
import com.sofkianos.consumer.dto.PagedKudoResponse;

public interface KudoService {

    void saveKudo(KudoEvent message);

    PagedKudoResponse searchKudos(KudoSearchCriteria criteria);
}
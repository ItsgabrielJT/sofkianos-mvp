package com.sofkianos.producer.application.usecase;

import com.sofkianos.producer.domain.model.Kudo;
import com.sofkianos.producer.domain.valueobject.KudoCategory;
import com.sofkianos.producer.domain.ports.out.KudoEventPublisher;
import com.sofkianos.producer.application.dto.KudoRequest;
import com.sofkianos.producer.application.dto.KudoResponse;
import com.sofkianos.producer.domain.exception.KudoNotFoundException;
import com.sofkianos.producer.application.exception.KudoMessagingException;
import com.sofkianos.producer.application.ports.in.KudoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Service — orchestrates the Kudo publishing workflow.
 * <p>
 * This class is <strong>free of infrastructure concerns</strong>:
 * <ul>
 * <li>No {@code RabbitTemplate} — messaging is delegated to
 * the {@link KudoEventPublisher} port.</li>
 * <li>No {@code ObjectMapper} — serialization lives in the adapter.</li>
 * </ul>
 * The service only knows about DTOs, domain events, and port interfaces.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KudoServiceImpl implements KudoService {
        private final KudoEventPublisher kudoEventPublisher;

        @Override
        public KudoResponse sendKudo(KudoRequest kudoRequest) {
                log.info("Processing Kudo: from={}, to={}", kudoRequest.getFrom(), kudoRequest.getTo());
                Kudo kudo = Kudo.create(
                                kudoRequest.getFrom(),
                                kudoRequest.getTo(),
                                KudoCategory.valueOf(kudoRequest.getCategory()),
                                kudoRequest.getMessage());
                Optional.of(kudo)
                                .map(e -> {
                                        kudoEventPublisher.publish(e);
                                        return e;
                                })
                                .orElseThrow(() -> new KudoMessagingException("Kudo was null"));

                String trackingId = UUID.randomUUID().toString();
                log.info("Kudo published successfully: from={}, to={}, trackingId={}",
                                kudo.fromUser(), kudo.toUser(), trackingId);

                return KudoResponse.builder()
                                .id(trackingId)
                                .message("Kudo queued successfully")
                                .status("ACCEPTED")
                                .timestamp(LocalDateTime.now())
                                .build();
        }
}

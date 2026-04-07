package com.hotelbooking.bookingservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.bookingservice.entity.OutboxEventEntity;
import com.hotelbooking.bookingservice.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {
    private static final String FALLBACK_TOPIC = "booking-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.relay.interval-ms:100}")
    @Transactional
    public void relay() {
        List<OutboxEventEntity> events = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEventEntity event : events) {
            try {
                JsonNode payloadNode = objectMapper.readTree(event.getPayload());
                String bookingId = payloadNode.path("bookingId").asText(null);
                if (bookingId == null || bookingId.isBlank()) {
                    bookingId = payloadNode.path("booking").path("bookingId").asText(null);
                }
                String topic = event.getTopic() == null || event.getTopic().isBlank() ? FALLBACK_TOPIC : event.getTopic();
                kafkaTemplate.send(topic, bookingId, event.getPayload()).get();

                event.setPublished(Boolean.TRUE);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception ex) {
                log.error("Failed to relay outbox event id={}", event.getId(), ex);
            }
        }
    }
}

package com.place_booking_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.place_booking_service.dto.CreateBooking;
import com.place_booking_service.entity.OutboxMessage;
import com.place_booking_service.repository.OutboxMessageRepository;
import com.place_booking_service.service.kafka.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OutboxPublisherService {

    @Autowired
    private  OutboxMessageRepository outboxMessageRepository;
    @Autowired
    private  KafkaProducerService kafkaProducerService;
    @Autowired
    private  ObjectMapper objectMapper;



    @Scheduled(fixedDelayString = "${outbox.publisher.delay:100}")
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pending = outboxMessageRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxMessage message : pending) {
            try {
                JsonNode payload = objectMapper.readTree(message.getPayload());
                String bookingId= payload.path("bookingId").asText();
                kafkaProducerService.send(message.getTopic(),bookingId, message.getPayload());

                message.setStatus("PROCESSED");
                message.setPublishedAt(LocalDateTime.now());

            } catch (Exception e) {
                log.error("Publish failed id={}", message.getId(), e);

                message.setRetryCount(message.getRetryCount() + 1);

                if (message.getRetryCount() >= 5) {
                    message.setStatus("FAILED");
                }
            }
        }

        outboxMessageRepository.saveAll(pending);
    }


    public void saveOutboxMessage(String topic, Object message,String eventType) {
        try{
            String payload = objectMapper.writeValueAsString(message);
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setId(UUID.randomUUID());
            outboxMessage.setEventType(eventType);
            outboxMessage.setTopic(topic);
            outboxMessage.setPayload(payload);
            outboxMessage.setStatus("PENDING");
            outboxMessage.setRetryCount(0);
            outboxMessage.setCreatedAt(LocalDateTime.now());
            outboxMessageRepository.save(outboxMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}

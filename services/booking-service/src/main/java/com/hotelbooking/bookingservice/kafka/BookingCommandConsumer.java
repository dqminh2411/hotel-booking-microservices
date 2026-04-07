package com.hotelbooking.bookingservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.bookingservice.dto.kafka.CancelBooking;
import com.hotelbooking.bookingservice.dto.kafka.ConfirmBooking;
import com.hotelbooking.bookingservice.dto.kafka.CreateBookingCommand;
import com.hotelbooking.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCommandConsumer {
    private final ObjectMapper objectMapper;
    private final BookingService bookingService;

    @KafkaListener(topics = "booking-commands")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();
            switch (eventType) {
                case "CreateBooking" -> bookingService.handleCreateBooking(objectMapper.treeToValue(node, CreateBookingCommand.class));
                case "ConfirmBooking" -> {
                    ConfirmBooking command = objectMapper.treeToValue(node, ConfirmBooking.class);
                    bookingService.handleConfirmBooking(command.sagaId(), command.bookingId());
                }
                case "CancelBooking" -> {
                    CancelBooking command = objectMapper.treeToValue(node, CancelBooking.class);
                    bookingService.handleCancelBooking(command.sagaId(), command.bookingId());
                }
                default -> log.warn("Ignore unsupported booking command eventType={}", eventType);
            }
        } catch (Exception ex) {
            log.error("Failed to consume booking command message={}", message, ex);
        }
    }
}

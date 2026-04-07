package com.hotelbooking.bookingservice.dto.kafka;

public record UpdateBookingCommand(
    String eventType,
    String bookingId
) {
}

package com.hotelbooking.bookingservice.dto.kafka;

public record ConfirmBooking(
    String sagaId,
    String eventType,
    String bookingId
) {
}


package com.hotelbooking.bookingservice.dto.kafka;

public record CancelBooking(
    String sagaId,
    String eventType,
    String bookingId
) {
}


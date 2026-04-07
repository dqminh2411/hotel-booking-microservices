package com.hotelbooking.bookingservice.dto.kafka;

import com.hotelbooking.bookingservice.dto.BookingDetail;

public record BookingFailed(
    String sagaId,
    String eventType,
    BookingDetail booking,
    String reason
) {
}


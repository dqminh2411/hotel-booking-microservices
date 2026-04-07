package com.hotelbooking.bookingservice.dto.kafka;

import com.hotelbooking.bookingservice.dto.BookingDetail;

public record BookingCancelled(
    String sagaId,
    String eventType,
    BookingDetail booking,
    String reason
) {
}


package com.hotelbooking.bookingservice.dto.kafka;

import com.hotelbooking.bookingservice.dto.BookingDetail;

public record BookingConfirmed(
    String sagaId,
    String eventType,
    BookingDetail booking
) {
}


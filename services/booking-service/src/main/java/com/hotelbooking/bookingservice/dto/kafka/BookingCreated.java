package com.hotelbooking.bookingservice.dto.kafka;

import com.hotelbooking.bookingservice.enums.PaymentMethod;
import java.math.BigDecimal;

public record BookingCreated(
    String sagaId,
    String eventType,
    String bookingId,
    String userId,
    BigDecimal totalAmount,
    String currency,
    PaymentMethod paymentMethod,
    String paymentToken
) {
}


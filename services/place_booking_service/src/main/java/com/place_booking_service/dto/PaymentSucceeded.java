package com.place_booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceeded {
    private String eventType; // "PaymentSucceeded"
    private String sagaId;
    private String bookingId;
    private String paymentId;
    private Double amount;
    private String currency;
    private String transactionRef;
    private LocalDateTime processedAt;
}

package com.payment_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefunded {
    private String eventType; // "PaymentRefunded"
    private String sagaId;
    private String bookingId;
    private String paymentId;
    private Double amount;
    private String currency;
    private LocalDateTime refundedAt;
}

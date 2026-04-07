package com.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPayment {
    private String eventType; // "ProcessPayment"
    private String sagaId;
    private String bookingId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String paymentToken;
    private String idempotencyKey;
    private String userId;
}

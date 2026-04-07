package com.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundPayment {
    private String eventType; // "RefundPayment"
    private String sagaId;
    private String bookingId;
    private String paymentId;
    private String reason;
}

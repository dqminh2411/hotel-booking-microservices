package com.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailed {
    private String eventType; // "PaymentFailed"
    private String sagaId;
    private String bookingId;
    private String reason;
}

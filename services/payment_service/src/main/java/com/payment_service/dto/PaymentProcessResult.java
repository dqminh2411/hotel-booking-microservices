package com.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessResult {
    private boolean success;
    private String paymentId;
    private String transactionRef;
    private String reason;
}
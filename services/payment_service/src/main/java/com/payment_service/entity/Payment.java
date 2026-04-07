package com.payment_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Payment {

    @Id
    private String paymentId;

    private String bookingId;
    private BigDecimal amount;
    private String currency;
    private String transactionRef;
    private LocalDateTime processedAt;
    private String idempotencyKey;
}

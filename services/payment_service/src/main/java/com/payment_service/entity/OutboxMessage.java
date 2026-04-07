package com.payment_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class OutboxMessage {

    @Id
    private UUID id;

    private String eventType;
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}

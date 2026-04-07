package com.place_booking_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class SagaState {

    @Id
    private String id;

    private String bookingId;

    private String idempotencyKey;

    private String status;
    private String currentStep;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

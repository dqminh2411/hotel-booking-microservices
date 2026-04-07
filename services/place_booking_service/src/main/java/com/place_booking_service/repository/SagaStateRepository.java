package com.place_booking_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.place_booking_service.entity.SagaState;

public interface SagaStateRepository extends JpaRepository<SagaState, UUID> {
    boolean existsSagaStateByIdempotencyKey(String idempotencyKey);

    SagaState findSagaStateByIdempotencyKey(String idempotencyKey);

    Optional<SagaState> findByBookingId(String bookingId);

    Optional<SagaState> findSagaStateById(String id);
}

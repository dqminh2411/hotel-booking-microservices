package com.place_booking_service.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.place_booking_service.entity.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {
    List<OutboxMessage> findTop50ByStatusOrderByCreatedAtAsc(String status);
}

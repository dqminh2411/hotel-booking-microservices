package com.payment_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment_service.entity.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {
    List<OutboxMessage> findTop50ByStatusOrderByCreatedAtAsc(String status);
}

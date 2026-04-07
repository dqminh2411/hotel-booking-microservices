package com.hotelbooking.bookingservice.repository;

import com.hotelbooking.bookingservice.entity.OutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}

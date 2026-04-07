package com.hotelbooking.bookingservice.repository;

import com.hotelbooking.bookingservice.entity.BookedRoomTypeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookedRoomTypeRepository extends JpaRepository<BookedRoomTypeEntity, String> {
    List<BookedRoomTypeEntity> findByBookingId(String bookingId);
}

package com.hotelbooking.hotelservice.repository;

import com.hotelbooking.hotelservice.entity.RoomTypeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomTypeEntity, String> {

    List<RoomTypeEntity> findByHotel_Id(String hotelId);

    Optional<RoomTypeEntity> findById(String roomTypeId);
}



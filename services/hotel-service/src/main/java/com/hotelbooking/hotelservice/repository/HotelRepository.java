package com.hotelbooking.hotelservice.repository;

import com.hotelbooking.hotelservice.entity.HotelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<HotelEntity, String> {

    Page<HotelEntity> findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(String name, String address, Pageable pageable);
}


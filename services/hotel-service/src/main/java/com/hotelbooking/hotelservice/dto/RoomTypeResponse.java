package com.hotelbooking.hotelservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record RoomTypeResponse(
        String roomTypeId,
        String hotelId,
        String name,
        String description,
        Integer capacity,
        Integer maxGuests,
        BigDecimal pricePerNight,
        List<String> imageUrls,
        Integer availableRooms
) {
}


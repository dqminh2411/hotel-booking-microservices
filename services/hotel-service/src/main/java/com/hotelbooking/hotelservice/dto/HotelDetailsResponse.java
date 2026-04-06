package com.hotelbooking.hotelservice.dto;

import java.util.List;

public record HotelDetailsResponse(
        String hotelId,
        String name,
        String address,
        Double starRating,
        String thumbnailUrl,
        String description,
        String phone,
        String email,
        List<String> imageUrls
) {
}


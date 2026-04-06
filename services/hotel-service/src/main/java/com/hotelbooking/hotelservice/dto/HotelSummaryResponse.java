package com.hotelbooking.hotelservice.dto;

public record HotelSummaryResponse(
        String hotelId,
        String name,
        String address,
        Double starRating,
        String thumbnailUrl
) {
}


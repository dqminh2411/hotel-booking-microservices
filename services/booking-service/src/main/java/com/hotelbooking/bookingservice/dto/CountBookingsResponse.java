package com.hotelbooking.bookingservice.dto;

import java.time.LocalDate;
import java.util.List;

public record CountBookingsResponse(
    String hotelId,
    LocalDate checkin,
    LocalDate checkout,
    List<ActiveBookingRoomType> activeBookingCount
) {
}

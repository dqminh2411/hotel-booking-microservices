package com.hotelbooking.hotelservice.dto;

import java.util.List;

public record BookingCountResponse(List<RoomTypeBookingCount> activeBookingCount) {
}

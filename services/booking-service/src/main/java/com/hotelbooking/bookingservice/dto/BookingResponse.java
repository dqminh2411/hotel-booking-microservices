package com.hotelbooking.bookingservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelbooking.bookingservice.enums.BookingStatus;

public record BookingResponse(
    String bookingId,
    BookingStatus status,
    JsonNode details
) {
}

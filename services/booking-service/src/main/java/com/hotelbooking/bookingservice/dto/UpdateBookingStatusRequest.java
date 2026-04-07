package com.hotelbooking.bookingservice.dto;

import com.hotelbooking.bookingservice.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBookingStatusRequest(
    @NotNull(message = "status is required")
    BookingStatus status
) {
}

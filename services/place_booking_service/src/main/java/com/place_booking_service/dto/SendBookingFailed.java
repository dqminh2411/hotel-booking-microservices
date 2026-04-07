package com.place_booking_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendBookingFailed {
    private String eventType;
    private String sagaId;
    private String bookingId;
    private String to;
    private BookingInfo booking;
    private String reason;
}

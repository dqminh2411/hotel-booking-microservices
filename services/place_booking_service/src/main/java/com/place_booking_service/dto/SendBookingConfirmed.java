package com.place_booking_service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendBookingConfirmed {
    private String eventType;
    private String sagaId;
    private String bookingId;
    private String to;
    private BookingInfo booking;

}

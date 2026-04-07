package com.place_booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailed {
    private String eventType;
    private String sagaId;
    private String bookingId;
    private String reason;
}

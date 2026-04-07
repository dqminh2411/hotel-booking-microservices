package com.place_booking_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingFailed {
    @NotBlank
    private String sagaId;
    @NotBlank private String eventType;
    private BookingInfo booking;
    private String reason;




}

package com.place_booking_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelBooking {


    @NotBlank
    private String sagaId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String bookingId;


}

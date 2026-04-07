package com.place_booking_service.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelled {
    @NotBlank
    private String sagaId;

    private String eventType;
    private BookingInfo booking;
    private String reason;


}

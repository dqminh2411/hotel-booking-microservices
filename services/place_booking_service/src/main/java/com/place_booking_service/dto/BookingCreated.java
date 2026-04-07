package com.place_booking_service.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreated {

    @NotBlank
    private String sagaId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String bookingId;
    @NotBlank
    private String userId;
    private Double totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentToken;


}

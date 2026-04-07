package com.place_booking_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBookingRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String hotelId;

    @NotEmpty
    private List<RoomType> roomTypeList;

    @NotBlank
    private String checkin;

    @NotBlank
    private String checkout;

    @Min(1)
    private int numAdults;

    @Positive
    private double totalAmount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String paymentToken;
}

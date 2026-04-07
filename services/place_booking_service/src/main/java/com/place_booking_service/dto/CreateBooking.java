package com.place_booking_service.dto;



import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBooking {
    @NotBlank
    private String sagaId;
    @NotBlank
    private String eventType;
    @NotNull
    private User user;
    @NotNull
    private Hotel hotel;
    @NotBlank
    private String bookingId;
    @NotNull
    private List<RoomType> roomTypeList;
    @NotBlank
    private String checkin;
    @NotBlank
    private String checkout;
    @Min(1)
    private int numAdults;
    @NotNull
    private Double totalAmount;
    @NotBlank
    private String currency;
    @NotBlank
    private String paymentMethod;
    @NotBlank
    private String paymentToken;

    public CreateBooking(PlaceBookingRequest placeBookingRequest) {
        this.checkin = placeBookingRequest.getCheckin();
        this.checkout = placeBookingRequest.getCheckout();
        this.currency = placeBookingRequest.getCurrency();
        this.numAdults = placeBookingRequest.getNumAdults();
        this.totalAmount= placeBookingRequest.getTotalAmount();
        this.paymentMethod = placeBookingRequest.getPaymentMethod();
        this.paymentToken = placeBookingRequest.getPaymentToken();
        this.roomTypeList = placeBookingRequest.getRoomTypeList();
    }





}

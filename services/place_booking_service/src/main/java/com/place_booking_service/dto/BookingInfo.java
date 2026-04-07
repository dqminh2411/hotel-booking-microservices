package com.place_booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfo {
    private String bookingId;
    private User customer;
    private String checkin;
    private String checkout;
    private int numAdults;
    private Double totalAmount;
    private Hotel hotel;
    private List<RoomType> roomTypeList;
}

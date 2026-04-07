package com.hotelbooking.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookingDetail {
    private String bookingId;
    private Customer customer;
    private String checkin;
    private String checkout;
    private int numAdults;
    private BigDecimal totalAmount;
    private Hotel hotel;
    private List<RoomType> roomTypeList;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {
        private String name;
        private String email;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Hotel{
        private String name;
        private String address;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomType{
        private String name;
        private int bedCount;
        private int bookingQuantity;
        private BigDecimal price;
    }
}

package com.hotelbooking.hotelservice.exception;

public class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(String hotelId) {
        super("Hotel not found: " + hotelId);
    }
}


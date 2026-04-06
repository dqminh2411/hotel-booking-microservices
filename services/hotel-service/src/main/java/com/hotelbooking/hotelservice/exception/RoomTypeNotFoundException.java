package com.hotelbooking.hotelservice.exception;

public class RoomTypeNotFoundException extends RuntimeException {

    public RoomTypeNotFoundException(String hotelId, String roomTypeId) {
        super("Room type not found: " + roomTypeId + " in hotel: " + hotelId);
    }
}


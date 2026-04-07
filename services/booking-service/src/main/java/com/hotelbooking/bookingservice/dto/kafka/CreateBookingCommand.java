package com.hotelbooking.bookingservice.dto.kafka;

import com.hotelbooking.bookingservice.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateBookingCommand(
    String sagaId,
    String eventType,
    User user,
    Hotel hotel,
    String bookingId,
    List<RoomTypeItem> roomTypeList,
    LocalDate checkin,
    LocalDate checkout,
    Integer numAdults,
    BigDecimal totalAmount,
    String currency,
    PaymentMethod paymentMethod,
    String paymentToken
) {
    public record User(String userId, String name, String email) {
    }

    public record Hotel(String hotelId, String name, String address) {
    }

    public record RoomTypeItem(
        String roomTypeId,
        String name,
        Integer bedCount,
        Integer bookingQuantity,
        Integer totalQuantity,
        BigDecimal price
    ) {
    }
}

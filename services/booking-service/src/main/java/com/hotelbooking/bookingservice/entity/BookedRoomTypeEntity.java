package com.hotelbooking.bookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booked_roomtypes")
public class BookedRoomTypeEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "room_type_id", nullable = false)
    private String roomTypeId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;
}

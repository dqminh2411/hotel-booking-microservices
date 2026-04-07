package com.hotelbooking.bookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booking_info")
public class BookingInfoEntity {
    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "booking_detail", nullable = false, columnDefinition = "jsonb")
    private String bookingDetail;
}

package com.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    @Email
    @NotBlank
    private String to;

    @NotBlank
    private String sagaId;

    @NotBlank
    private String eventType;

    private String bookingId;

    private BookingInfo booking;

    private String reason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        private String bookingId;
        private User customer;
        private String checkin;
        private String checkout;
        private Integer numAdults;
        private Double totalAmount;
        private Hotel hotel;
        private List<RoomType> roomTypeList;


    }
}

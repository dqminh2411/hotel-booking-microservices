package com.notification_service.service;

import com.notification_service.dto.EmailTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateService {

    public String buildContent(EmailTemplate template, Map<String, Object> data) {

        return switch (template) {
            case BOOKING_CONFIRMED, SendBookingConfirmed -> """
                Xin chào %s,

                Đặt phòng của bạn đã được xác nhận!
                Booking ID: %s
                Khách sạn: %s
                Phòng: %s
                Checkin: %s
                Checkout: %s

                Tổng tiền: %s VND
                """.formatted(
                data.get("guestName"),
                data.get("bookingId"),
                data.get("hotelName"),
                data.get("roomType"),
                data.get("checkin"),
                data.get("checkout"),
                data.get("totalPrice")
            );

            case BOOKING_CANCELLED, SendBookingFailed -> """
                Xin chào %s,

                Đặt phòng của bạn đã bị huỷ.
                Booking ID: %s
                Khách sạn: %s
                Lý do: %s
                """.formatted(
                data.get("guestName"),
                data.get("bookingId"),
                data.get("hotelName"),
                data.get("reason")
            );
        };
    }
}

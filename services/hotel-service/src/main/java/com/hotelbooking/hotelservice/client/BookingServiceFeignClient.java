package com.hotelbooking.hotelservice.client;

import java.time.LocalDate;
import java.util.List;

import com.hotelbooking.hotelservice.dto.BookingCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", path = "/bookings")
public interface BookingServiceFeignClient {

    @GetMapping("/count")
    BookingCountResponse countBookings(
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false, name = "roomTypeList") List<String> roomTypeList,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    );
}


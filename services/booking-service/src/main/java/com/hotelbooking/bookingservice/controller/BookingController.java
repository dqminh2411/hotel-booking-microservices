package com.hotelbooking.bookingservice.controller;

import com.hotelbooking.bookingservice.dto.BookingResponse;
import com.hotelbooking.bookingservice.dto.CountBookingsResponse;
import com.hotelbooking.bookingservice.dto.UpdateBookingStatusRequest;
import com.hotelbooking.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/count")
    public CountBookingsResponse countBookings(
        @RequestParam(required = false) String hotelId,
        @RequestParam(required = false) List<String> roomTypeList,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return bookingService.countBookings(hotelId, roomTypeList, checkin, checkout);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable String bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PatchMapping("/{bookingId}/status")
    public BookingResponse updateBookingStatus(@PathVariable String bookingId, @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.updateBookingStatus(bookingId, request.status());
    }
}

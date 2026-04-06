package com.hotelbooking.hotelservice.controller;

import com.hotelbooking.hotelservice.dto.HotelDetailsResponse;
import com.hotelbooking.hotelservice.dto.HotelSummaryResponse;
import com.hotelbooking.hotelservice.dto.PagedResponse;
import com.hotelbooking.hotelservice.dto.RoomTypeResponse;
import com.hotelbooking.hotelservice.service.HotelService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotels")
@Validated
@RequiredArgsConstructor
public class HotelController {

    private static final String ID_PATTERN = "^[A-Za-z0-9-]+$";

    private final HotelService hotelService;

    @GetMapping
    public PagedResponse<HotelSummaryResponse> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return hotelService.searchHotels(name, address, page, size);
    }

    @GetMapping("/{hotelId}")
    public HotelDetailsResponse getHotelById(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "hotelId has invalid format")
            String hotelId
    ) {
        return hotelService.getHotelById(hotelId);
    }

    @GetMapping("/{hotelId}/room-types")
    public List<RoomTypeResponse> getRoomTypesByHotel(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "hotelId has invalid format")
            String hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return hotelService.getRoomTypesByHotel(hotelId, checkin, checkout);
    }

    @GetMapping("/{hotelId}/room-types/{roomTypeId}")
    public RoomTypeResponse getRoomTypeById(
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "hotelId has invalid format")
            String hotelId,
            @PathVariable
            @Pattern(regexp = ID_PATTERN, message = "roomTypeId has invalid format")
            String roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return hotelService.getRoomTypeById(hotelId, roomTypeId, checkin, checkout);
    }
}


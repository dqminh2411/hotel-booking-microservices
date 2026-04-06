package com.hotelbooking.hotelservice.service;

import com.hotelbooking.hotelservice.dto.HotelDetailsResponse;
import com.hotelbooking.hotelservice.dto.HotelSummaryResponse;
import com.hotelbooking.hotelservice.dto.PagedResponse;
import com.hotelbooking.hotelservice.dto.RoomTypeResponse;
import java.time.LocalDate;
import java.util.List;

public interface HotelService {

    PagedResponse<HotelSummaryResponse> searchHotels(String name, String address, int page, int size);

    HotelDetailsResponse getHotelById(String hotelId);

    List<RoomTypeResponse> getRoomTypesByHotel(String hotelId, LocalDate checkin, LocalDate checkout);

    RoomTypeResponse getRoomTypeById(String hotelId, String roomTypeId, LocalDate checkin, LocalDate checkout);
}


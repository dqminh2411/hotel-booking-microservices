package com.place_booking_service.client;

import com.place_booking_service.dto.Hotel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotel-service")
public interface HotelServiceClient {

    @GetMapping("/hotels/{id}")
    public Hotel getHotelById(@PathVariable("id") String id);
}

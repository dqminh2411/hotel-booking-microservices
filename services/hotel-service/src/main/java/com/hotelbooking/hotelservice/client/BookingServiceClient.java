package com.hotelbooking.hotelservice.client;

import com.hotelbooking.hotelservice.dto.BookingCountResponse;
import com.hotelbooking.hotelservice.exception.ExternalServiceException;
import feign.FeignException;
import feign.RetryableException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingServiceClient {

    private final BookingServiceFeignClient bookingServiceFeignClient;

    public Map<String, Integer> countActiveBookingsByRoomType(String hotelId, List<String> roomTypeIds, LocalDate checkin,
            LocalDate checkout) {
        if (roomTypeIds.isEmpty()) {
            return Map.of();
        }

        try {
            BookingCountResponse response = bookingServiceFeignClient.countBookings(
                    hotelId,
                    roomTypeIds,
                    checkin,
                    checkout
            );

            if (response == null || response.activeBookingCount() == null) {
                return Map.of();
            }

            Map<String, Integer> result = new HashMap<>();
            response.activeBookingCount().forEach(item -> result.put(item.roomTypeId(), item.count().intValue()));
            return result;
        } catch (RetryableException ex) {
            log.error("Booking-service is unavailable while counting active bookings", ex);
            throw new ExternalServiceException("BOOKING_SERVICE_UNAVAILABLE", "Booking service is unavailable");
        } catch (FeignException ex) {
            log.warn("Booking-service returned error status {} while counting active bookings", ex.status());
            throw new ExternalServiceException("BOOKING_SERVICE_ERROR", "Booking service returned an error");
        }
    }
}


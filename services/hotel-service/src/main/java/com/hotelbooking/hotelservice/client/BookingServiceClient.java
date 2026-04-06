package com.hotelbooking.hotelservice.client;

import com.hotelbooking.hotelservice.exception.ExternalServiceException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingServiceClient {

    private final RestTemplate restTemplate;

    @Value("${booking-service.url:http://booking-service:5004}")
    private String bookingServiceUrl;

    public Map<String, Integer> countActiveBookingsByRoomType(String hotelId, List<String> roomTypeIds, LocalDate checkin,
            LocalDate checkout) {
        if (roomTypeIds.isEmpty()) {
            return Map.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(bookingServiceUrl)
                .path("/bookings/count")
                .queryParam("hotelId", hotelId)
                .queryParam("roomTypeList", roomTypeIds.toArray())
                .queryParam("checkin", checkin)
                .queryParam("checkout", checkout)
                .toUriString();

        try {
            BookingCountResponse response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<BookingCountResponse>() {
                    }
            ).getBody();

            if (response == null || response.activeBookingCount() == null) {
                return Map.of();
            }

            Map<String, Integer> result = new HashMap<>();
            response.activeBookingCount().forEach(item -> result.put(item.roomTypeId(), item.count()));
            return result;
        } catch (RestClientResponseException ex) {
            log.warn("Booking-service returned error status {} for URL {}", ex.getStatusCode(), url);
            throw new ExternalServiceException("BOOKING_SERVICE_ERROR", "Booking service returned an error");
        } catch (RestClientException ex) {
            log.error("Failed to call booking-service at {}", url, ex);
            throw new ExternalServiceException("BOOKING_SERVICE_UNAVAILABLE", "Booking service is unavailable");
        }
    }

    private record BookingCountResponse(List<RoomTypeBookingCount> activeBookingCount) {
    }

    private record RoomTypeBookingCount(String roomTypeId, Integer count) {
    }
}


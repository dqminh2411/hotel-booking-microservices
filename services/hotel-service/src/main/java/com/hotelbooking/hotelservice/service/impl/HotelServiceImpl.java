package com.hotelbooking.hotelservice.service.impl;

import com.hotelbooking.hotelservice.client.BookingServiceClient;
import com.hotelbooking.hotelservice.dto.HotelDetailsResponse;
import com.hotelbooking.hotelservice.dto.HotelSummaryResponse;
import com.hotelbooking.hotelservice.dto.PagedResponse;
import com.hotelbooking.hotelservice.dto.RoomTypeResponse;
import com.hotelbooking.hotelservice.entity.HotelEntity;
import com.hotelbooking.hotelservice.entity.RoomTypeEntity;
import com.hotelbooking.hotelservice.exception.HotelNotFoundException;
import com.hotelbooking.hotelservice.exception.InvalidDateRangeException;
import com.hotelbooking.hotelservice.exception.RoomTypeNotFoundException;
import com.hotelbooking.hotelservice.repository.HotelRepository;
import com.hotelbooking.hotelservice.repository.RoomTypeRepository;
import com.hotelbooking.hotelservice.service.HotelService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingServiceClient bookingServiceClient;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HotelSummaryResponse> searchHotels(String name, String address, int page, int size) {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedAddress = address == null ? "" : address.trim();

        Page<HotelEntity> hotels = hotelRepository.findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(
                normalizedName,
                normalizedAddress,
                PageRequest.of(page, size)
        );

        List<HotelSummaryResponse> data = hotels.getContent().stream()
                .map(this::toHotelSummary)
                .toList();

        return new PagedResponse<>(data, hotels.getTotalElements(), hotels.getNumber(), hotels.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDetailsResponse getHotelById(String hotelId) {
        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        return toHotelDetails(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getRoomTypesByHotel(String hotelId, LocalDate checkin, LocalDate checkout) {
        validateAvailabilityInput(checkin, checkout);
        ensureHotelExists(hotelId);

        List<RoomTypeEntity> roomTypes = roomTypeRepository.findByHotel_Id(hotelId);
        if (roomTypes.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> bookedCountByRoomType = getBookedCountByRoomType(hotelId, checkin, checkout, roomTypes);

        return roomTypes.stream()
                .map(roomType -> toRoomTypeResponse(roomType, bookedCountByRoomType.get(roomType.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(String hotelId, String roomTypeId, LocalDate checkin, LocalDate checkout) {
        validateAvailabilityInput(checkin, checkout);
        ensureHotelExists(hotelId);

        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(hotelId, roomTypeId));

        Integer bookedCount = getBookedCountByRoomType(
                hotelId,
                checkin,
                checkout,
                List.of(roomType)
        ).get(roomType.getId());

        return toRoomTypeResponse(roomType, bookedCount);
    }

    private void ensureHotelExists(String hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
    }

    private void validateAvailabilityInput(LocalDate checkin, LocalDate checkout) {
        if (checkin == null && checkout == null) {
            return;
        }

        if (checkin == null || checkout == null) {
            throw new InvalidDateRangeException("checkin and checkout must be provided together");
        }

        if (checkin.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("checkin cannot be in the past");
        }

        if (!checkout.isAfter(checkin)) {
            throw new InvalidDateRangeException("checkout must be after checkin");
        }
    }

    private Map<String, Integer> getBookedCountByRoomType(String hotelId, LocalDate checkin, LocalDate checkout,
            List<RoomTypeEntity> roomTypes) {
        if (checkin == null || checkout == null) {
            return Collections.emptyMap();
        }

        List<String> roomTypeIds = roomTypes.stream().map(RoomTypeEntity::getId).toList();
        return bookingServiceClient.countActiveBookingsByRoomType(hotelId, roomTypeIds, checkin, checkout);
    }

    private HotelSummaryResponse toHotelSummary(HotelEntity entity) {
        return new HotelSummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                null,
                entity.getImageUrl()
        );
    }

    private HotelDetailsResponse toHotelDetails(HotelEntity entity) {
        List<String> imageUrls = entity.getImageUrl() == null ? List.of() : List.of(entity.getImageUrl());

        return new HotelDetailsResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                null,
                entity.getImageUrl(),
                entity.getDescription(),
                null,
                null,
                imageUrls
        );
    }

    private RoomTypeResponse toRoomTypeResponse(RoomTypeEntity entity, Integer bookedCount) {
        Integer availableRooms = bookedCount == null ? null : Math.max(entity.getQuantity() - bookedCount, 0);
        List<String> imageUrls = entity.getImageUrl() == null ? List.of() : List.of(entity.getImageUrl());

        return new RoomTypeResponse(
                entity.getId(),
                entity.getHotel().getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getQuantity(),
                entity.getMaxGuests(),
                entity.getBasePricePerNight(),
                imageUrls,
                availableRooms
        );
    }
}




package com.hotelbooking.hotelservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hotelbooking.hotelservice.client.BookingServiceClient;
import com.hotelbooking.hotelservice.dto.PagedResponse;
import com.hotelbooking.hotelservice.dto.RoomTypeResponse;
import com.hotelbooking.hotelservice.entity.HotelEntity;
import com.hotelbooking.hotelservice.entity.RoomTypeEntity;
import com.hotelbooking.hotelservice.exception.HotelNotFoundException;
import com.hotelbooking.hotelservice.exception.InvalidDateRangeException;
import com.hotelbooking.hotelservice.repository.HotelRepository;
import com.hotelbooking.hotelservice.repository.RoomTypeRepository;
import com.hotelbooking.hotelservice.service.impl.HotelServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingServiceClient bookingServiceClient;

    private HotelServiceImpl hotelService;

    @BeforeEach
    void setUp() {
        hotelService = new HotelServiceImpl(hotelRepository, roomTypeRepository, bookingServiceClient);
    }

    @Test
    void searchHotelsReturnsPagedResults() {
        HotelEntity hotel = new HotelEntity();
        hotel.setId("HT-001");
        hotel.setName("Marriott Hanoi");
        hotel.setAddress("Hanoi");
        hotel.setImageUrl("https://img");
        hotel.setCreatedAt(Instant.now());

        Page<HotelEntity> page = new PageImpl<>(List.of(hotel), PageRequest.of(0, 10), 1);
        when(hotelRepository.findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase("Marriott", "Hanoi", PageRequest.of(0, 10)))
                .thenReturn(page);

        PagedResponse<?> response = hotelService.searchHotels("Marriott", "Hanoi", 0, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.data()).hasSize(1);
    }

    @Test
    void getHotelByIdThrowsWhenHotelMissing() {
        when(hotelRepository.findById("HT-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById("HT-999"))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void getRoomTypesByHotelComputesAvailabilityWhenDateProvided() {
        HotelEntity hotel = new HotelEntity();
        hotel.setId("HT-001");

        RoomTypeEntity roomType = new RoomTypeEntity();
        roomType.setId("RT-001");
        roomType.setHotel(hotel);
        roomType.setName("Superior Room");
        roomType.setMaxGuests(2);
        roomType.setQuantity(10);
        roomType.setBasePricePerNight(BigDecimal.valueOf(100));

        when(hotelRepository.existsById("HT-001")).thenReturn(true);
        when(roomTypeRepository.findByHotel_Id("HT-001")).thenReturn(List.of(roomType));
        when(bookingServiceClient.countActiveBookingsByRoomType(any(), any(), any(), any()))
                .thenReturn(Map.of("RT-001", 3));

        List<RoomTypeResponse> result = hotelService.getRoomTypesByHotel(
                "HT-001",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3)
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().availableRooms()).isEqualTo(7);
    }

    @Test
    void getRoomTypesByHotelRejectsInvalidDateRange() {
        assertThatThrownBy(() -> hotelService.getRoomTypesByHotel(
                "HT-001",
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(1)
        )).isInstanceOf(InvalidDateRangeException.class);
    }
}



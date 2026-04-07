package com.hotelbooking.bookingservice.repository;

import com.hotelbooking.bookingservice.dto.ActiveBookingRoomType;
import com.hotelbooking.bookingservice.entity.BookingEntity;
import com.hotelbooking.bookingservice.enums.BookingStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    @Query("""
        select b from BookingEntity b
        where b.id = :bookingId
    """)
    Optional<BookingEntity> findByBookingId(@Param("bookingId") String bookingId);

    @Query("""
        select new com.hotelbooking.bookingservice.dto.ActiveBookingRoomType(br.roomTypeId, coalesce(sum(br.quantity), 0))
        from BookedRoomTypeEntity br, BookingEntity b
        where br.bookingId = b.id
          and (:hotelId is null or b.hotelId = :hotelId)
          and (:roomTypeIdsEmpty = true or br.roomTypeId in (:roomTypeIds))
          and b.status in (:activeStatuses)
          and b.checkinDate < :checkout
          and b.checkoutDate > :checkin
        group by br.roomTypeId
        """)
    List<ActiveBookingRoomType> countActiveBookingsByRoomType(
        @Param("hotelId") String hotelId,
        @Param("roomTypeIds") Collection<String> roomTypeIds,
        @Param("roomTypeIdsEmpty") boolean roomTypeIdsEmpty,
        @Param("checkin") LocalDate checkin,
        @Param("checkout") LocalDate checkout,
        @Param("activeStatuses") Collection<BookingStatus> activeStatuses
    );
}

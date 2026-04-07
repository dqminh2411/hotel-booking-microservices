package com.hotelbooking.bookingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.bookingservice.dto.BookingResponse;
import com.hotelbooking.bookingservice.dto.CountBookingsResponse;
import com.hotelbooking.bookingservice.dto.kafka.CreateBookingCommand;
import com.hotelbooking.bookingservice.entity.BookedRoomTypeEntity;
import com.hotelbooking.bookingservice.entity.BookingEntity;
import com.hotelbooking.bookingservice.entity.BookingInfoEntity;
import com.hotelbooking.bookingservice.entity.OutboxEventEntity;
import com.hotelbooking.bookingservice.enums.BookingStatus;
import com.hotelbooking.bookingservice.exception.AppException;
import com.hotelbooking.bookingservice.repository.BookedRoomTypeRepository;
import com.hotelbooking.bookingservice.repository.BookingInfoRepository;
import com.hotelbooking.bookingservice.repository.BookingRepository;
import com.hotelbooking.bookingservice.repository.OutboxEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final String OUTBOX_TOPIC = "booking-events";
    private static final Pattern BOOKING_ID_PATTERN = Pattern.compile("^BK-[A-Za-z0-9-]+$");
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final BookedRoomTypeRepository bookedRoomTypeRepository;
    private final BookingInfoRepository bookingInfoRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public BookingResponse getBookingById(String bookingId) {
        validateBookingId(bookingId);
        BookingEntity booking = bookingRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new AppException("BOOKING_NOT_FOUND", "Booking does not exist", HttpStatus.NOT_FOUND));

        JsonNode details = null;
        if (bookingInfoRepository.existsById(bookingId)) {
            BookingInfoEntity bookingInfo = bookingInfoRepository.findById(bookingId).orElse(null);
            if (bookingInfo != null) {
                try {
                    details = objectMapper.readTree(bookingInfo.getBookingDetail());
                } catch (Exception ignore) {
                    details = null;
                }
            }
        }

        return new BookingResponse(booking.getId(), booking.getStatus(), details);
    }

    public CountBookingsResponse countBookings(String hotelId, List<String> roomTypeList, LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new AppException("VALIDATION_ERROR", "checkin and checkout are required", HttpStatus.BAD_REQUEST);
        }
        if (!checkout.isAfter(checkin)) {
            throw new AppException("VALIDATION_ERROR", "checkout must be after checkin", HttpStatus.BAD_REQUEST);
        }
        if ((hotelId == null || hotelId.isBlank()) && (roomTypeList == null || roomTypeList.isEmpty())) {
            throw new AppException("VALIDATION_ERROR", "hotelId or roomTypeList is required", HttpStatus.BAD_REQUEST);
        }

        List<String> safeRoomTypeList = roomTypeList == null ? List.of() : roomTypeList;
        List<BookingRepository.ActiveBookingProjection> rows = bookingRepository.countActiveBookingsByRoomType(
            blankToNull(hotelId),
            safeRoomTypeList,
            safeRoomTypeList.isEmpty(),
            checkin,
            checkout,
            ACTIVE_STATUSES.stream().map(Enum::name).toList()
        );

        List<CountBookingsResponse.RoomTypeCount> activeBookingCount = rows.stream()
            .map(it -> new CountBookingsResponse.RoomTypeCount(it.getRoomTypeId(), it.getBookingCount()))
            .toList();

        return new CountBookingsResponse(hotelId, checkin, checkout, activeBookingCount);
    }

    @Transactional
    public BookingResponse updateBookingStatus(String bookingId, BookingStatus newStatus) {
        validateBookingId(bookingId);
        BookingEntity booking = bookingRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new AppException("BOOKING_NOT_FOUND", "Booking does not exist", HttpStatus.NOT_FOUND));

        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus == newStatus) {
            return getBookingById(bookingId);
        }

        boolean validTransition =
            (currentStatus == BookingStatus.PENDING && (newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.FAILED))
                || (currentStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.CANCELLED);
        if (!validTransition) {
            throw new AppException(
                "INVALID_STATUS_TRANSITION",
                "Cannot transition from " + currentStatus + " to " + newStatus,
                HttpStatus.BAD_REQUEST
            );
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        saveOutboxEvent(newStatus == BookingStatus.CONFIRMED ? "BookingConfirmed" : "BookingCancelled", booking.getId(), null);
        return getBookingById(bookingId);
    }

    @Transactional
    public void handleCreateBooking(CreateBookingCommand command) {
        validateCreateCommand(command);
        if (bookingRepository.existsById(command.bookingId())) {
            return;
        }

        Map<String, CreateBookingCommand.RoomTypeItem> roomTypeMap = command.roomTypeList().stream()
            .collect(Collectors.toMap(CreateBookingCommand.RoomTypeItem::roomTypeId, Function.identity()));

        List<BookingRepository.ActiveBookingProjection> activeRows = bookingRepository.countActiveBookingsByRoomType(
            command.hotel().hotelId(),
            new ArrayList<>(roomTypeMap.keySet()),
            roomTypeMap.isEmpty(),
            command.checkin(),
            command.checkout(),
            ACTIVE_STATUSES.stream().map(Enum::name).toList()
        );
        Map<String, Long> activeByRoomType = activeRows.stream()
            .collect(Collectors.toMap(BookingRepository.ActiveBookingProjection::getRoomTypeId, BookingRepository.ActiveBookingProjection::getBookingCount));

        for (CreateBookingCommand.RoomTypeItem item : command.roomTypeList()) {
            long existing = activeByRoomType.getOrDefault(item.roomTypeId(), 0L);
            long expected = existing + item.bookingQuantity();
            if (expected > item.totalQuantity()) {
                saveOutboxEvent("BookingFailed", command.bookingId(), "Room " + item.name() + " not available");
                return;
            }
        }

        BookingEntity booking = new BookingEntity();
        booking.setId(command.bookingId());
        booking.setCustomerId(command.user().userId());
        booking.setHotelId(command.hotel().hotelId());
        booking.setCreatedAt(Instant.now());
        booking.setCheckinDate(command.checkin());
        booking.setCheckoutDate(command.checkout());
        booking.setNumAdults(command.numAdults());
        booking.setTotalAmount(command.totalAmount());
        booking.setCurrency(command.currency());
        booking.setPaymentMethod(command.paymentMethod());
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        long nights = ChronoUnit.DAYS.between(command.checkin(), command.checkout());
        for (CreateBookingCommand.RoomTypeItem item : command.roomTypeList()) {
            BookedRoomTypeEntity entity = new BookedRoomTypeEntity();
            entity.setId("BR-" + UUID.randomUUID());
            entity.setBookingId(command.bookingId());
            entity.setRoomTypeId(item.roomTypeId());
            entity.setQuantity(item.bookingQuantity());
            entity.setPricePerNight(item.price());
            entity.setNights((int) nights);
            entity.setSubtotal(item.price().multiply(BigDecimal.valueOf(item.bookingQuantity())).multiply(BigDecimal.valueOf(nights)));
            bookedRoomTypeRepository.save(entity);
        }

        try {
            BookingInfoEntity info = new BookingInfoEntity();
            info.setBookingId(command.bookingId());
            info.setBookingDetail(objectMapper.writeValueAsString(command));
            bookingInfoRepository.save(info);
        } catch (Exception ex) {
            throw new AppException("INTERNAL_SERVER_ERROR", "Failed to persist booking detail", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        saveOutboxEvent("BookingCreated", command.bookingId(), null);
    }

    @Transactional
    public void handleConfirmBooking(String bookingId) {
        updateStatusFromCommand(bookingId, BookingStatus.CONFIRMED, "BookingConfirmed");
    }

    @Transactional
    public void handleCancelBooking(String bookingId) {
        updateStatusFromCommand(bookingId, BookingStatus.CANCELLED, "BookingCancelled");
    }

    private void updateStatusFromCommand(String bookingId, BookingStatus targetStatus, String eventType) {
        validateBookingId(bookingId);
        BookingEntity booking = bookingRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new AppException("BOOKING_NOT_FOUND", "Booking does not exist", HttpStatus.NOT_FOUND));
        booking.setStatus(targetStatus);
        bookingRepository.save(booking);
        saveOutboxEvent(eventType, bookingId, null);
    }

    private void saveOutboxEvent(String eventType, String bookingId, String reason) {
        try {
            JsonNode payload = objectMapper.createObjectNode()
                .put("eventType", eventType)
                .put("bookingId", bookingId);
            if (reason != null) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) payload).put("reason", reason);
            }

            OutboxEventEntity outbox = new OutboxEventEntity();
            outbox.setId(UUID.randomUUID());
            outbox.setTopic(OUTBOX_TOPIC);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setPublished(Boolean.FALSE);
            outbox.setCreatedAt(Instant.now());
            outboxEventRepository.save(outbox);
        } catch (Exception ex) {
            throw new AppException("INTERNAL_SERVER_ERROR", "Failed to write outbox event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateCreateCommand(CreateBookingCommand command) {
        if (command == null
            || command.user() == null
            || command.hotel() == null
            || command.roomTypeList() == null
            || command.roomTypeList().isEmpty()
            || command.checkin() == null
            || command.checkout() == null
            || command.numAdults() == null
            || command.totalAmount() == null
            || command.currency() == null
            || command.paymentMethod() == null) {
            throw new AppException("VALIDATION_ERROR", "CreateBooking payload is invalid", HttpStatus.BAD_REQUEST);
        }
        validateBookingId(command.bookingId());
        if (!command.checkout().isAfter(command.checkin())) {
            throw new AppException("VALIDATION_ERROR", "checkout must be after checkin", HttpStatus.BAD_REQUEST);
        }
        if (command.numAdults() <= 0) {
            throw new AppException("VALIDATION_ERROR", "numAdults must be positive", HttpStatus.BAD_REQUEST);
        }

        for (CreateBookingCommand.RoomTypeItem item : command.roomTypeList()) {
            if (item.roomTypeId() == null || item.bookingQuantity() == null || item.totalQuantity() == null || item.price() == null) {
                throw new AppException("VALIDATION_ERROR", "Invalid roomTypeList item", HttpStatus.BAD_REQUEST);
            }
            if (item.bookingQuantity() <= 0 || item.totalQuantity() <= 0) {
                throw new AppException("VALIDATION_ERROR", "room quantities must be positive", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validateBookingId(String bookingId) {
        if (bookingId == null || !BOOKING_ID_PATTERN.matcher(bookingId).matches()) {
            throw new AppException("VALIDATION_ERROR", "bookingId must match BK-UUID format", HttpStatus.BAD_REQUEST);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

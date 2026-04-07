package com.place_booking_service.service;


import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.place_booking_service.dto.CreateBooking;
import com.place_booking_service.dto.Hotel;
import com.place_booking_service.dto.PlaceBookingRequest;
import com.place_booking_service.dto.User;
import com.place_booking_service.entity.OutboxMessage;
import com.place_booking_service.entity.SagaState;
import com.place_booking_service.repository.OutboxMessageRepository;
import com.place_booking_service.repository.SagaStateRepository;

@Service
public class PlaceBookingService {

    @Autowired
    SagaStateRepository sagaStateRepository;



    @Autowired
    OutboxPublisherService outboxPublisherService;



    @Transactional
    public String startSaga(PlaceBookingRequest placeBookingRequest, User user, Hotel hotel) {

        if(sagaStateRepository.existsSagaStateByIdempotencyKey(placeBookingRequest.getIdempotencyKey())){
            return sagaStateRepository.findSagaStateByIdempotencyKey(placeBookingRequest.getIdempotencyKey()).getBookingId();
        }

        CreateBooking createBooking = new CreateBooking(placeBookingRequest);
        createBooking.setUser(user);
        createBooking.setHotel(hotel);
        String bookingId = "BK-" + UUID.randomUUID().toString();
        createBooking.setBookingId(bookingId);
        String sagaId = UUID.randomUUID().toString();
        createBooking.setSagaId(sagaId);
        createBooking.setEventType("CreateBooking");

        SagaState sagaState = new SagaState();
        sagaState.setId(sagaId);
        sagaState.setIdempotencyKey(placeBookingRequest.getIdempotencyKey());
        sagaState.setBookingId(bookingId);
        sagaState.setStatus("IN_PROGRESS");
        sagaState.setCurrentStep("STARTED");
        sagaState.setCreatedAt(LocalDateTime.now());
        sagaState.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(sagaState);

        outboxPublisherService.saveOutboxMessage("booking-commands" ,createBooking, createBooking.getEventType());

        return bookingId;
    }



}


package com.place_booking_service.service.kafka;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.place_booking_service.dto.*;
import com.place_booking_service.service.OutboxPublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.place_booking_service.entity.SagaState;
import com.place_booking_service.repository.SagaStateRepository;

@Service
public class KafkaConsumerService {

    @Autowired
    SagaStateRepository sagaStateRepository;




    @Autowired
    OutboxPublisherService outboxPublisherService;

    @KafkaListener(topics = "booking-events")
    @Transactional
    public void bookingEventsHandler(String payloadJson) throws JsonProcessingException {
        Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
        String eventType = (String) payload.get("eventType");
        String sagaId = (String) payload.get("sagaId");
        Optional<SagaState> state= sagaStateRepository.findSagaStateById(sagaId);
        if(state.isEmpty()){
            return;
        }
        ObjectMapper mapper = new ObjectMapper();

        switch (eventType) {
            case "BookingCreated" -> handleBookingCreated(mapper.convertValue(payload, BookingCreated.class));
            case "BookingConfirmed" -> handleBookingConfirmed(mapper.convertValue(payload, BookingConfirmed.class));
            case "BookingFailed" -> handleBookingFailed(mapper.convertValue(payload, BookingFailed.class));
            case "BookingCancelled" -> handleBookingCancelled(mapper.convertValue(payload, BookingCancelled.class));
        }
    }
    @KafkaListener(topics = "booking-events")
    @Transactional
    public void paymentEventsHandler(String payloadJson) throws JsonProcessingException {
        Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
        String eventType = (String) payload.get("eventType");
        String sagaId = (String) payload.get("sagaId");
        Optional<SagaState> state= sagaStateRepository.findSagaStateById(sagaId);
        if(state.isEmpty()){
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        switch (eventType) {
            case "PaymentSucceeded"-> handlePaymentSucceeded(mapper.convertValue(payload, PaymentSucceeded.class)) ;
            case "PaymentFailed"-> handlePaymentFailed(mapper.convertValue(payload, PaymentFailed.class));
        }
    }


    private void handleBookingCreated(BookingCreated bookingCreated) {
        System.out.println(bookingCreated);

        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingCreated.getBookingId());
        if (sagaOpt.isEmpty()) {
            return; // Saga not found, skip
        }



        SagaState saga = sagaOpt.get();
        saga.setCurrentStep("BOOKING_CREATED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);

        // Publish ProcessPayment command
        ProcessPayment processPayment = new ProcessPayment();
        processPayment.setEventType("ProcessPayment");
        processPayment.setSagaId(saga.getId());
        processPayment.setBookingId(bookingCreated.getBookingId());
        processPayment.setAmount(bookingCreated.getTotalAmount());
        processPayment.setCurrency(bookingCreated.getCurrency());
        processPayment.setPaymentMethod(bookingCreated.getPaymentMethod());
        processPayment.setPaymentToken(bookingCreated.getPaymentToken());
        processPayment.setIdempotencyKey(saga.getIdempotencyKey());
        processPayment.setUserId(bookingCreated.getUserId());

        outboxPublisherService.saveOutboxMessage("payment-commands",processPayment,"ProcessPayment");



    }

    private void handleBookingConfirmed(BookingConfirmed bookingConfirmed) {

        System.out.println(bookingConfirmed);

        String bookingId = bookingConfirmed.getBooking().getBookingId();
        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingId);

        System.out.println(sagaOpt.isPresent());
        if (sagaOpt.isEmpty()) {
            return; // Saga not found, skip
        }


        SagaState saga = sagaOpt.get();
        saga.setStatus("CONFIRMED");
        saga.setCurrentStep("COMPLETED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);

        // Publish SendNotification command for BOOKING_CONFIRMED
        if (bookingConfirmed.getBooking().getCustomer() != null
            && bookingConfirmed.getBooking().getCustomer().getEmail() != null) {
            SendBookingConfirmed sendBookingConfirmed = new SendBookingConfirmed();
            sendBookingConfirmed.setBookingId(bookingId);
            sendBookingConfirmed.setTo(bookingConfirmed.getBooking().getCustomer().getEmail());
            sendBookingConfirmed.setSagaId(saga.getId());
            sendBookingConfirmed.setBooking(bookingConfirmed.getBooking());
            sendBookingConfirmed.setEventType("SendBookingConfirmed");
            outboxPublisherService.saveOutboxMessage("notification-commands",sendBookingConfirmed,"SendBookingConfirmed");

        }
    }

    private void handleBookingFailed(BookingFailed bookingFailed) {

        System.out.println(bookingFailed);
        String bookingId = bookingFailed.getBooking().getBookingId();
        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingId);

        if (sagaOpt.isEmpty()) {
            return;
        }

        SagaState saga = sagaOpt.get();
        saga.setStatus("FAILED");
        saga.setCurrentStep("BOOKING_FAILED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);

        // Publish SendNotification command for BOOKING_CANCELLED
        if (bookingFailed.getBooking().getCustomer() != null
            && bookingFailed.getBooking().getCustomer().getEmail() != null) {
            SendBookingFailed sendBookingFailed = new SendBookingFailed();
            sendBookingFailed.setBookingId(bookingId);
            sendBookingFailed.setTo(bookingFailed.getBooking().getCustomer().getEmail());
            sendBookingFailed.setSagaId(saga.getId());
            sendBookingFailed.setBooking(bookingFailed.getBooking());
            sendBookingFailed.setEventType("SendBookingFailed");
            outboxPublisherService.saveOutboxMessage("notification-commands",sendBookingFailed,"SendBookingFailed");

        }
    }

    private void handleBookingCancelled(BookingCancelled bookingCancelled) {

        System.out.println(bookingCancelled);

        String bookingId = bookingCancelled.getBooking().getBookingId();
        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingId);

        if (sagaOpt.isEmpty()) {
            return; // Saga not found, skip
        }

        SagaState saga = sagaOpt.get();
        saga.setStatus("CANCELLED");
        saga.setCurrentStep("BOOKING_CANCELLED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);

        // Publish SendNotification command for BOOKING_CANCELLED
        if (bookingCancelled.getBooking().getCustomer() != null
            && bookingCancelled.getBooking().getCustomer().getEmail() != null) {
            SendBookingFailed sendBookingFailed = new SendBookingFailed();
            sendBookingFailed.setBookingId(bookingId);
            sendBookingFailed.setTo(bookingCancelled.getBooking().getCustomer().getEmail());
            sendBookingFailed.setSagaId(saga.getId());
            sendBookingFailed.setBooking(bookingCancelled.getBooking());
            sendBookingFailed.setEventType("SendBookingFailed");
            outboxPublisherService.saveOutboxMessage("notification-commands",sendBookingFailed,"SendBookingFailed");
        }
    }

    private void handlePaymentSucceeded(PaymentSucceeded paymentSucceeded) {
        System.out.println(paymentSucceeded);
        String bookingId = paymentSucceeded.getBookingId();
        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingId);
        if (sagaOpt.isEmpty()) {
            return;
        }
        SagaState saga = sagaOpt.get();
        saga.setStatus("PAYMENT_SUCCEEDED");
        saga.setCurrentStep("PAYMENT_SUCCEEDED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);
        ConfirmBooking confirmBooking = new ConfirmBooking();
        confirmBooking.setBookingId(bookingId);
        confirmBooking.setEventType("ConfirmBooking");
        confirmBooking.setSagaId(saga.getId());
        outboxPublisherService.saveOutboxMessage("booking-commands",confirmBooking,"ConfirmBooking");
    }

    private void handlePaymentFailed(PaymentFailed paymentFailed) {
        System.out.println(paymentFailed);
        String bookingId = paymentFailed.getBookingId();
        Optional<SagaState> sagaOpt = sagaStateRepository.findByBookingId(bookingId);
        if (sagaOpt.isEmpty()) {
            return;
        }
        SagaState saga = sagaOpt.get();
        saga.setStatus("PAYMENT_FAILED");
        saga.setCurrentStep("PAYMENT_FAILED");
        saga.setUpdatedAt(LocalDateTime.now());
        sagaStateRepository.save(saga);
        CancelBooking cancelBooking = new CancelBooking();
        cancelBooking.setBookingId(bookingId);
        cancelBooking.setEventType("CancelBooking");
        cancelBooking.setSagaId(saga.getId());
        outboxPublisherService.saveOutboxMessage("booking-commands",cancelBooking,"CancelBooking");

    }





}


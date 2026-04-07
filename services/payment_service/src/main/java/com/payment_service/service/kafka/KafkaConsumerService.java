package com.payment_service.service.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_service.dto.PaymentFailed;
import com.payment_service.dto.PaymentProcessResult;
import com.payment_service.dto.PaymentRefunded;
import com.payment_service.dto.PaymentSucceeded;
import com.payment_service.dto.ProcessPayment;
import com.payment_service.dto.RefundPayment;
import com.payment_service.entity.Payment;
import com.payment_service.repository.PaymentRepository;
import com.payment_service.service.OutboxPublisherService;
import com.payment_service.service.PaymentService;

@Service
public class KafkaConsumerService {

    @Autowired
    private  PaymentService paymentService;
    @Autowired
    private  OutboxPublisherService outboxPublisherService;
    @Autowired
    private PaymentRepository paymentRepository;



    @KafkaListener(topics = "payment-commands")
    public void paymentCommandsHandler(String payloadJson) throws JsonProcessingException {
        Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
        String eventType = (String) payload.get("eventType");
        System.out.println(eventType);

        ObjectMapper objectMapper = new ObjectMapper();
        switch (eventType) {
            case "ProcessPayment" -> handleProcessPayment(objectMapper.convertValue(payload, ProcessPayment.class));

        }
    }

    private void handleProcessPayment(ProcessPayment processPayment) {
        PaymentProcessResult result = paymentService.processPayment(processPayment);
        System.out.println(processPayment);
        System.out.println(result);

        if (result.isSuccess()) {
            // Save payment to DB
            Payment payment = new Payment();
            payment.setPaymentId(result.getPaymentId());
            payment.setBookingId(processPayment.getBookingId());
            payment.setAmount(BigDecimal.valueOf(processPayment.getAmount()));
            payment.setCurrency(processPayment.getCurrency());
            payment.setTransactionRef(result.getTransactionRef());
            payment.setIdempotencyKey(processPayment.getIdempotencyKey());
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);


            PaymentSucceeded paymentSucceeded = new PaymentSucceeded();
            paymentSucceeded.setEventType("PaymentSucceeded");
            paymentSucceeded.setSagaId(processPayment.getSagaId());
            paymentSucceeded.setBookingId(processPayment.getBookingId());
            paymentSucceeded.setPaymentId(result.getPaymentId());
            paymentSucceeded.setTransactionRef(result.getTransactionRef());

            paymentSucceeded.setAmount(processPayment.getAmount());
            paymentSucceeded.setCurrency(processPayment.getCurrency());
            paymentSucceeded.setTransactionRef(result.getTransactionRef());
            paymentSucceeded.setProcessedAt(LocalDateTime.now());

            outboxPublisherService.saveOutboxMessage("payment-events", paymentSucceeded, "PaymentSucceeded");
        } else {

            PaymentFailed paymentFailed = new PaymentFailed();
            paymentFailed.setEventType("PaymentFailed");
            paymentFailed.setSagaId(processPayment.getSagaId());
            paymentFailed.setBookingId(processPayment.getBookingId());
            paymentFailed.setReason(result.getReason());

            outboxPublisherService.saveOutboxMessage("payment-events", paymentFailed, "PaymentFailed");
        }
    }

}

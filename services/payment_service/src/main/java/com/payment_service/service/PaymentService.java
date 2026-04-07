package com.payment_service.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.payment_service.dto.PaymentProcessResult;
import com.payment_service.dto.PaymentRefunded;
import com.payment_service.dto.ProcessPayment;
import com.payment_service.dto.RefundPayment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class PaymentService {

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "paymentGatewayFallback")
    public PaymentProcessResult processPayment(ProcessPayment processPayment) {

        // Mock payment gateway: 90% success
        boolean success = ThreadLocalRandom.current().nextInt(100) < 90;

        if (!success) {
            // THROW để CircuitBreaker nhận biết failure
            throw new RuntimeException("Payment gateway declined the transaction");
        }

        String paymentId = "PAY-" + UUID.randomUUID();
        String transactionRef = "TXN-" + System.currentTimeMillis();

        return new PaymentProcessResult(true, paymentId, transactionRef, null);
    }

    // fallback cho processPayment
    public PaymentProcessResult paymentGatewayFallback(ProcessPayment processPayment, Throwable t) {
        return new PaymentProcessResult(
            false,
            null,
            null,
            "Payment failed (fallback): " + t.getMessage()
        );
    }




}

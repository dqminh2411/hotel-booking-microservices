package com.payment_service.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent to " + topic + ": " + message);
    }

    public void send(String topic, String key, Object message) {
        kafkaTemplate.send(topic, key, message);
        System.out.println("Sent to " + topic + " with key " + key + ": " + message);
    }
}

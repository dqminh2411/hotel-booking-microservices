package com.notification_service.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.EmailRequest;
import com.notification_service.dto.EmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationConsumerService {
    @Autowired
    EmailService emailService;
    @Autowired
    TemplateService templateService;


    @KafkaListener(topics = "notification-commands", groupId = "notification-service-group")
    public void consume(String payloadJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        EmailRequest command = objectMapper.readValue(payloadJson, EmailRequest.class);
        System.out.println(command);

        String content = templateService.buildContent(
            EmailTemplate.valueOf(command.getEventType()),
            command
        );
        emailService.send(command.getTo(),"Notification",content);

    }
}

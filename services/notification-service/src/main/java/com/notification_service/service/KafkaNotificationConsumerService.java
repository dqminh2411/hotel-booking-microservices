package com.notification_service.service;


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
    public void consume(EmailRequest command) {
        System.out.println("Received notification command: " + command.getEventType() +
            " for " + command.getTo());

        String content = templateService.buildContent(
            EmailTemplate.valueOf(command.getEventType()),
            command.getData()
        );
        emailService.send(command.getTo(),"Notification",content);

    }
}

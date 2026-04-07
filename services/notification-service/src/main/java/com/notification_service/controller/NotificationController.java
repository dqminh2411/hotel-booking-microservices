package com.notification_service.controller;

import com.notification_service.dto.EmailRequest;
import com.notification_service.dto.EmailTemplate;
import com.notification_service.service.EmailService;
import com.notification_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;
    private final TemplateService templateService;

    @PostMapping("/email")
    public ResponseEntity<?> send(@RequestBody EmailRequest req) {
        try{
            String content = templateService.buildContent(
                EmailTemplate.valueOf(req.getEventType()),
                req.getData()
            );

            emailService.send(
                req.getTo(),
                "Notification",
                content
            );

            return ResponseEntity.accepted().body(
                Map.of(
                    "status", "QUEUED",
                    "message", "Email sẽ được gửi đến " + req.getTo()
                )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "code", "EMAIL_SEND_FAILED",
                    "message", e.getMessage()
                )
            );
        }


    }
}

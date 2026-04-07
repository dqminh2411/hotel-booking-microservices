package com.notification_service.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/actuator/health");
    }
}

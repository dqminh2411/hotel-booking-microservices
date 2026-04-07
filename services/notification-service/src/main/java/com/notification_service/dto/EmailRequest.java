package com.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class EmailRequest {

    @Email
    @NotBlank
    private String to;

    @NotBlank
    private String eventType;

    @NotNull
    private Map<String, Object> data;
}

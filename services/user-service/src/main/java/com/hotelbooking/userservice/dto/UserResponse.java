package com.hotelbooking.userservice.dto;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        String userId,
        String name,
        String email,
        String phone,
        Instant createdAt,
        List<String> roles
) {
}

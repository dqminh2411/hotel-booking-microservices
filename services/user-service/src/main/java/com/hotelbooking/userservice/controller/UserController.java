package com.hotelbooking.userservice.controller;

import com.hotelbooking.userservice.dto.UserResponse;
import com.hotelbooking.userservice.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable
            @NotBlank(message = "userId is required")
            @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "userId has invalid format")
            String userId
    ) {
        return userService.getUserById(userId);
    }
}


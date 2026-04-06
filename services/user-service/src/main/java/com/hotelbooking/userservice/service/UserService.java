package com.hotelbooking.userservice.service;

import com.hotelbooking.userservice.dto.UserResponse;

public interface UserService {

    UserResponse getUserById(String userId);
}


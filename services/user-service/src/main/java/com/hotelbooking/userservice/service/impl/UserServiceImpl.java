package com.hotelbooking.userservice.service.impl;

import com.hotelbooking.userservice.dto.UserResponse;
import com.hotelbooking.userservice.entity.UserEntity;
import com.hotelbooking.userservice.exception.UserNotFoundException;
import com.hotelbooking.userservice.repository.UserRepository;
import com.hotelbooking.userservice.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getUserById(String userId) {
        UserEntity user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .distinct()
                .sorted()
                .toList();

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                roles
        );
    }
}

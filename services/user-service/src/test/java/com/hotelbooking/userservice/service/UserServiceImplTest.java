package com.hotelbooking.userservice.service;

import com.hotelbooking.userservice.entity.RoleEntity;
import com.hotelbooking.userservice.entity.UserEntity;
import com.hotelbooking.userservice.exception.UserNotFoundException;
import com.hotelbooking.userservice.repository.UserRepository;
import com.hotelbooking.userservice.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserServiceImplTest {

    @Test
    void getUserByIdShouldReturnRoles() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        RoleEntity role = new RoleEntity();
        role.setId("RO-001");
        role.setName("CUSTOMER");

        UserEntity user = new UserEntity();
        user.setId("US-001");
        user.setName("Nguyen Van An");
        user.setEmail("an.nguyen@email.com");
        user.setPhone("0901234561");
        user.setCreatedAt(Instant.parse("2024-01-15T08:30:00Z"));
        user.setRoles(Set.of(role));

        Mockito.when(userRepository.findWithRolesById("US-001")).thenReturn(java.util.Optional.of(user));

        var response = userService.getUserById("US-001");

        Assertions.assertEquals("US-001", response.userId());
        Assertions.assertEquals(1, response.roles().size());
        Assertions.assertEquals("CUSTOMER", response.roles().get(0));
    }

    @Test
    void getUserByIdShouldThrowWhenUserDoesNotExist() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        Mockito.when(userRepository.findWithRolesById("US-404")).thenReturn(java.util.Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getUserById("US-404"));
    }
}


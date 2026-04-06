package com.hotelbooking.userservice.repository;

import com.hotelbooking.userservice.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findWithRolesById(String id);
}

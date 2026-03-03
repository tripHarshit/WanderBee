package com.wanderbee.identityservice.repository;

import com.wanderbee.identityservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Integer> {
    Optional<UserCredentials> findByName(String name);
    Optional<UserCredentials> findByEmail(String email);
}

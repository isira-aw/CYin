package com.example.RegistrationLoginPage.repository;

import com.example.RegistrationLoginPage.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Optional<Work> findFirstByCustomerId(int customerId);
}

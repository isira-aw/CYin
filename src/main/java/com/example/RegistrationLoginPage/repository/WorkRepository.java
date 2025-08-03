package com.example.RegistrationLoginPage.repository;

import com.example.RegistrationLoginPage.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Optional<Work> findFirstByCustomerId(int customerId);
    public List<Work> findByCustomerIdAndDateBetween(int customerId, LocalDate startDate, LocalDate endDate);

}

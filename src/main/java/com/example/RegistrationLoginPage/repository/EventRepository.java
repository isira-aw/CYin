package com.example.RegistrationLoginPage.repository;

import com.example.RegistrationLoginPage.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCustomerIdAndDate(int customerId, LocalDate date);
}
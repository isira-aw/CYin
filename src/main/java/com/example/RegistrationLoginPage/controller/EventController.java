package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.EventRequest;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Event;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<CommonResponseDTO> logEvent(@RequestBody EventRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // extracted from JWT

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = new Event();
        event.setCustomer(customer);
        event.setStatus(request.getStatus());
        event.setLocation(request.getLocation());
        event.setDate(LocalDate.now());
        event.setTime(LocalTime.now());

        eventRepository.save(event);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Event logged successfully.");
        response.setData(request);

        return ResponseEntity.ok(response);
    }
}

package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.EventRequest;
import com.example.RegistrationLoginPage.dto.WorkRequest;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Event;
import com.example.RegistrationLoginPage.entity.Work;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.EventRepository;
import com.example.RegistrationLoginPage.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import com.example.RegistrationLoginPage.dto.WorkRequest;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Work;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/newevent")
    public ResponseEntity<String> logEvent(@RequestBody EventRequest request) {
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

        return ResponseEntity.ok("Event logged successfully.");
    }


    @Autowired
    private WorkRepository workRepository;

    @PostMapping(path = "/work")
    public ResponseEntity<String> logWork(@RequestBody WorkRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Work work = new Work();
        work.setCustomer(customer);
        work.setDescription(request.getDescription());
        work.setDate(LocalDate.now());

        workRepository.save(work);

        return ResponseEntity.ok("Work logged successfully.");
    }
}

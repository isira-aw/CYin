package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.WorkRequest;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Work;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/work")
public class WorkController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WorkRepository workRepository;

    @PostMapping
    public ResponseEntity<CommonResponseDTO> logWork(@RequestBody WorkRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Work work = new Work();
        work.setCustomer(customer);
        work.setDescription(request.getDescription());
        work.setDate(LocalDate.now());

        workRepository.save(work);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Work logged successfully.");
        response.setData(null);

        return ResponseEntity.ok(response);
    }
}

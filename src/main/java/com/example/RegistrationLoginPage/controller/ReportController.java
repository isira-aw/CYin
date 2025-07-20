package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.UserReportResponse;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Event;
import com.example.RegistrationLoginPage.entity.Work;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.EventRepository;
import com.example.RegistrationLoginPage.repository.WorkRepository;
import com.example.RegistrationLoginPage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public ResponseEntity<CommonResponseDTO> getUserReport(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Work> work = workRepository.findFirstByCustomerId(customer.getId());
        List<Event> events = eventRepository.findByCustomerIdAndDate(customer.getId(), date);

        UserReportResponse response = new UserReportResponse();
        response.setCustomerName(customer.getCustomerName());
        response.setRole(customer.getRole());
        response.setDescription(work.map(Work::getDescription).orElse("No work logged"));

        List<UserReportResponse.ActivityLog> activityLogs = events.stream()
                .map(e -> new UserReportResponse.ActivityLog(
                        e.getTime(), e.getLocation(), e.getStatus()))
                .collect(Collectors.toList());

        response.setActivities(activityLogs);

        CommonResponseDTO result = new CommonResponseDTO();
        result.setStatus(true);
        result.setMessage("Report fetched successfully.");
        result.setData(response);

        return ResponseEntity.ok(result);
    }


    @Autowired
    private CustomerService customerService;

    @GetMapping(path = "/customers")
    public List<Customer> getAllUsers() {
        return (List<Customer>) customerService.getAllEmployee();
    }
}

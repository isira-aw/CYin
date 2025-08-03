package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.DateRangeRequest;
import com.example.RegistrationLoginPage.dto.UserReportRequestDTO;
import com.example.RegistrationLoginPage.dto.UserReportResponse;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.entity.Event;
import com.example.RegistrationLoginPage.entity.Work;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.repository.EventRepository;
import com.example.RegistrationLoginPage.repository.WorkRepository;
import com.example.RegistrationLoginPage.service.CustomerService;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    private CustomerService customerService;

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
        response.setReportDate(date);
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

    @GetMapping("/generate-report")
    public ResponseEntity<byte[]> generateUserReportPdf(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // If startDate or endDate is not provided, set them to today's date
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today;
            endDate = today;
        }

        // Validate that startDate is before or equal to endDate
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("End date cannot be earlier than start date.");
        }

        // Fetch the customer using the email
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch works and events within the date range
        List<Work> works = workRepository.findByCustomerIdAndDateBetween(customer.getId(), startDate, endDate);
        List<Event> events = eventRepository.findByCustomerIdAndDateBetween(customer.getId(), startDate, endDate);

        // Generate PDF report
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Title
            document.add(new Paragraph("User Report: " + customer.getCustomerName()).setFontSize(18).setBold());
            document.add(new Paragraph("Role: " + customer.getRole()).setFontSize(14));
            document.add(new Paragraph("Report Period: " + startDate + " to " + endDate).setFontSize(14));

            // Work Table
            document.add(new Paragraph("Work Details:").setFontSize(16).setBold());
            if (works.isEmpty()) {
                document.add(new Paragraph("No work logs available for this period."));
            } else {
                Table workTable = new Table(2);
                workTable.addCell("Date");
                workTable.addCell("Description");
                for (Work work : works) {
                    workTable.addCell(work.getDate().toString());
                    workTable.addCell(work.getDescription());
                }
                document.add(workTable);
            }

            // Event Table
            document.add(new Paragraph("Event Details:").setFontSize(16).setBold());
            if (events.isEmpty()) {
                document.add(new Paragraph("No events available for this period."));
            } else {
                Table eventTable = new Table(3);
                eventTable.addCell("Date");
                eventTable.addCell("Location");
                eventTable.addCell("Status");
                for (Event event : events) {
                    eventTable.addCell(event.getDate().toString());
                    eventTable.addCell(event.getLocation());
                    eventTable.addCell(event.getStatus());
                }
                document.add(eventTable);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-report.pdf")
                .body(byteArrayOutputStream.toByteArray());
    }


    @GetMapping("/generate-all-users-report")
    public ResponseEntity<byte[]> generateAllUsersReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // If startDate or endDate is not provided, set them to today's date
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today;
            endDate = today;
        }

        // Validate that startDate is before or equal to endDate
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("End date cannot be earlier than start date.");
        }

        // Fetch all customers
        List<Customer> customers = customerRepository.findAll();

        // Prepare list of emails that should be included in the report
        List<String> emailsWithData = new ArrayList<>();

        // Generate PDF report
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Title
            document.add(new Paragraph("All Users Report").setFontSize(18).setBold());

            // Loop through all customers and generate content for each
            for (Customer customer : customers) {
                List<Work> works = workRepository.findByCustomerIdAndDateBetween(customer.getId(), startDate, endDate);
                List<Event> events = eventRepository.findByCustomerIdAndDateBetween(customer.getId(), startDate, endDate);

                if (!works.isEmpty() || !events.isEmpty()) {
                    // Add Customer Name and Email as labels
                    document.add(new Paragraph("Customer Name: " + customer.getCustomerName()).setFontSize(14).setBold());
                    document.add(new Paragraph("Email: " + customer.getEmail()).setFontSize(14).setBold());

                    // Work Table
                    document.add(new Paragraph("Work Details:").setFontSize(16).setBold());
                    if (works.isEmpty()) {
                        document.add(new Paragraph("No work logs available for this period."));
                    } else {
                        Table workTable = new Table(2);
                        workTable.addCell("Date");
                        workTable.addCell("Description");
                        for (Work work : works) {
                            workTable.addCell(work.getDate().toString());
                            workTable.addCell(work.getDescription());
                        }
                        document.add(workTable);
                    }

                    // Event Table
                    document.add(new Paragraph("Event Details:").setFontSize(16).setBold());
                    if (events.isEmpty()) {
                        document.add(new Paragraph("No events available for this period."));
                    } else {
                        Table eventTable = new Table(3);
                        eventTable.addCell("Date");
                        eventTable.addCell("Location");
                        eventTable.addCell("Status");
                        for (Event event : events) {
                            eventTable.addCell(event.getDate().toString());
                            eventTable.addCell(event.getLocation());
                            eventTable.addCell(event.getStatus());
                        }
                        document.add(eventTable);
                    }

                    // Add a space between users' data
                    document.add(new Paragraph("\n"));
                }
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all-users-report.pdf")
                .body(byteArrayOutputStream.toByteArray());
    }

    @PostMapping("/generate-user-report-json")
    public ResponseEntity<Map<String, Object>> generateUserReportJson(
            @RequestBody UserReportRequestDTO userReportRequest) {

        // Validate that startDate is before or equal to endDate
        if (userReportRequest.getStartDate().isAfter(userReportRequest.getEndDate())) {
            throw new RuntimeException("End date cannot be earlier than start date.");
        }

        // Fetch the customer using the email
        Customer customer = customerRepository.findByEmail(userReportRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch works and events within the date range
        List<Work> works = workRepository.findByCustomerIdAndDateBetween(customer.getId(),
                userReportRequest.getStartDate(),
                userReportRequest.getEndDate());
        List<Event> events = eventRepository.findByCustomerIdAndDateBetween(customer.getId(),
                userReportRequest.getStartDate(),
                userReportRequest.getEndDate());

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("customerName", customer.getCustomerName());
        response.put("email", customer.getEmail());
        response.put("role", customer.getRole());
        response.put("reportPeriod", userReportRequest.getStartDate() + " to " + userReportRequest.getEndDate());

        // Work details
        List<Map<String, String>> workDetails = new ArrayList<>();
        for (Work work : works) {
            Map<String, String> workData = new HashMap<>();
            workData.put("date", work.getDate().toString());
            workData.put("description", work.getDescription());
            workDetails.add(workData);
        }
        response.put("workDetails", workDetails);

        // Event details
        List<Map<String, String>> eventDetails = new ArrayList<>();
        for (Event event : events) {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("date", event.getDate().toString());
            eventData.put("location", event.getLocation());
            eventData.put("status", event.getStatus());
            eventDetails.add(eventData);
        }
        response.put("eventDetails", eventDetails);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-all-users-report-json")
    public ResponseEntity<List<Map<String, Object>>> generateAllUsersReportJson(
            @RequestBody DateRangeRequest dateRangeRequest) {

        // Validate that startDate is before or equal to endDate
        if (dateRangeRequest.getStartDate().isAfter(dateRangeRequest.getEndDate())) {
            throw new RuntimeException("End date cannot be earlier than start date.");
        }

        // Fetch all customers
        List<Customer> customers = customerRepository.findAll();

        // Prepare the response list
        List<Map<String, Object>> allUsersReports = new ArrayList<>();

        // Loop through all customers and generate content for each
        for (Customer customer : customers) {
            List<Work> works = workRepository.findByCustomerIdAndDateBetween(customer.getId(),
                    dateRangeRequest.getStartDate(),
                    dateRangeRequest.getEndDate());
            List<Event> events = eventRepository.findByCustomerIdAndDateBetween(customer.getId(),
                    dateRangeRequest.getStartDate(),
                    dateRangeRequest.getEndDate());

            if (!works.isEmpty() || !events.isEmpty()) {
                // Prepare individual user report
                Map<String, Object> userReport = new HashMap<>();
                userReport.put("customerName", customer.getCustomerName());
                userReport.put("email", customer.getEmail());

                // Work details
                List<Map<String, String>> workDetails = new ArrayList<>();
                for (Work work : works) {
                    Map<String, String> workData = new HashMap<>();
                    workData.put("date", work.getDate().toString());
                    workData.put("description", work.getDescription());
                    workDetails.add(workData);
                }
                userReport.put("workDetails", workDetails);

                // Event details
                List<Map<String, String>> eventDetails = new ArrayList<>();
                for (Event event : events) {
                    Map<String, String> eventData = new HashMap<>();
                    eventData.put("date", event.getDate().toString());
                    eventData.put("location", event.getLocation());
                    eventData.put("status", event.getStatus());
                    eventDetails.add(eventData);
                }
                userReport.put("eventDetails", eventDetails);

                // Add the user report to the list
                allUsersReports.add(userReport);
            }
        }

        return ResponseEntity.ok(allUsersReports);
    }


    @GetMapping(path = "/customers")
    public List<Customer> getAllUsers() {
        return (List<Customer>) customerService.getAllEmployee();
    }
}

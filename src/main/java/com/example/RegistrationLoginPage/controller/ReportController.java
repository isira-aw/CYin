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
import com.example.RegistrationLoginPage.service.GeocodingService;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Autowired
    private GeocodingService geocodingService;

    @GetMapping("/generate-report")
    public ResponseEntity<byte[]> generateUserReportPdf(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        long startTime = System.currentTimeMillis(); // Start time measurement

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

            // New Table for Date, Working Hours, OT Time, Actual Working Time
            document.add(new Paragraph("Work Summary:").setFontSize(15).setBold());
            Table summaryTable = new Table(4);
            summaryTable.addCell("Date");
            summaryTable.addCell("Working Hours");
            summaryTable.addCell("OT Time");
            summaryTable.addCell("Actual Working Time");

            // Initialize a map to track actual working times (number of "starting working" events per day)
            Map<LocalDate, Integer> actualWorkingTimeMap = new HashMap<>();

            for (Event event : events) {
                if ("starting working".equals(event.getStatus()) || "ending".equals(event.getStatus())) {
                    // Parse event times
                    LocalDate eventDate = event.getDate();
                    LocalTime eventTime = event.getTime();  // Assume this is LocalTime

                    // Convert LocalTime to LocalDateTime (use a placeholder date if necessary)
                    LocalDateTime startDateTime = LocalDateTime.of(eventDate, eventTime);

                    // Find the corresponding end time for the start event (you need to implement this logic)
                    LocalDateTime endDateTime = getEventEndTimeForStart(event, events);

                    long workingHours = 0; // Default working hours if both are not available
                    if (endDateTime != null) {
                        workingHours = Duration.between(startDateTime, endDateTime).toHours();
                    }

                    // Calculate OT Time (working hours minus 8 hours)
                    long otTime = Math.max(0, workingHours - 8);

                    // Track actual working time
                    actualWorkingTimeMap.put(eventDate, actualWorkingTimeMap.getOrDefault(eventDate, 0) + 1);

                    // Add a row to the summary table for the current event
                    summaryTable.addCell(eventDate.toString());
                    summaryTable.addCell(String.valueOf(workingHours));
                    summaryTable.addCell(String.valueOf(otTime));
                    summaryTable.addCell(String.valueOf(actualWorkingTimeMap.get(eventDate)));
                }
            }

            // Add the summary table to the document
            document.add(summaryTable);

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
                Table eventTable = new Table(4); // Added an extra column for status and time
                eventTable.addCell("Date");
                eventTable.addCell("Location");
                eventTable.addCell("Status");
                eventTable.addCell("Time");

                for (Event event : events) {
                    // If the event location contains coordinates, replace it with the address
                    String location = event.getLocation();
                    if (location.contains(",")) {
                        String[] coords = location.split(",");
                        double latitude = Double.parseDouble(coords[0]);
                        double longitude = Double.parseDouble(coords[1]);
                        location = geocodingService.getAddressFromCoordinates(latitude, longitude);
                    }

                    eventTable.addCell(event.getDate().toString());
                    eventTable.addCell(location);  // Add the replaced address
                    eventTable.addCell(event.getStatus());
                    eventTable.addCell(event.getTime().toString());
                }

                document.add(eventTable);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis(); // End time measurement
        long duration = endTime - startTime; // Calculate time taken

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-report.pdf")
                .header("X-Generation-Time", duration + "ms")  // Add custom header with generation time
                .body(byteArrayOutputStream.toByteArray());
    }

    /**
     * Finds the corresponding "end working" event for a given "start working" event.
     * @param startEvent The start working event.
     * @param events List of all events.
     * @return The corresponding end working event's time.
     */
    private LocalDateTime getEventEndTimeForStart(Event startEvent, List<Event> events) {
        // Find the event with the same date but "ending" status after the start event
        for (Event event : events) {
            if ("ending".equals(event.getStatus()) && event.getDate().equals(startEvent.getDate())) {
                return LocalDateTime.of(event.getDate(), event.getTime());
            }
        }
        return null; // Return null if no corresponding end event is found
    }



    @GetMapping("/generate-all-users-report")
    public ResponseEntity<byte[]> generateAllUsersReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        long startTime = System.currentTimeMillis(); // Start time measurement

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
                        Table eventTable = new Table(4); // Added an extra column for status and time
                        eventTable.addCell("Date");
                        eventTable.addCell("Location");
                        eventTable.addCell("Status");
                        eventTable.addCell("Time");

                        for (Event event : events) {
                            // Replace coordinates with actual address if found
                            String location = event.getLocation();
                            if (location.contains(",")) {
                                String[] coords = location.split(",");
                                double latitude = Double.parseDouble(coords[0]);
                                double longitude = Double.parseDouble(coords[1]);
                                location = geocodingService.getAddressFromCoordinates(latitude, longitude);
                            }

                            eventTable.addCell(event.getDate().toString());
                            eventTable.addCell(location);  // Add the replaced address
                            eventTable.addCell(event.getStatus());
                            eventTable.addCell(event.getTime().toString());
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

        long endTime = System.currentTimeMillis(); // End time measurement
        long duration = endTime - startTime; // Calculate time taken

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all-users-report.pdf")
                .header("X-Generation-Time", duration + "ms")  // Add custom header with generation time
                .body(byteArrayOutputStream.toByteArray());
    }

    // Endpoint to generate a user report in JSON format
    @PostMapping("/generate-user-report-json")
    public ResponseEntity<Map<String, Object>> generateUserReportJson(
            @RequestBody UserReportRequestDTO userReportRequest) {

        long startTime = System.currentTimeMillis(); // Start time measurement

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

//        // Work details
//        List<Map<String, String>> workDetails = new ArrayList<>();
//        for (Work work : works) {
//            Map<String, String> workData = new HashMap<>();
//            workData.put("date", work.getDate().toString());
//            workData.put("description", work.getDescription());
//            workDetails.add(workData);
//        }
//        response.put("workDetails", workDetails);

        // Event details
        List<Map<String, String>> eventDetails = new ArrayList<>();
        for (Event event : events) {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("date", event.getDate().toString());
            eventData.put("location", event.getLocation());
            eventData.put("status", event.getStatus());
            eventData.put("time", event.getTime().toString());  // Added time
            eventDetails.add(eventData);
        }
        response.put("eventDetails", eventDetails);

//        long endTime = System.currentTimeMillis(); // End time measurement
//        long duration = endTime - startTime; // Calculate time taken
//
//        response.put("generationTime", duration + "ms"); // Include time in response

        return ResponseEntity.ok(response);
    }

    // Endpoint to generate reports for all users in JSON format
    @PostMapping("/generate-all-users-report-json")
    public ResponseEntity<List<Map<String, Object>>> generateAllUsersReportJson(
            @RequestBody DateRangeRequest dateRangeRequest) {

        long startTime = System.currentTimeMillis(); // Start time measurement

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

//                // Work details
//                List<Map<String, String>> workDetails = new ArrayList<>();
//                for (Work work : works) {
//                    Map<String, String> workData = new HashMap<>();
//                    workData.put("date", work.getDate().toString());
//                    workData.put("description", work.getDescription());
//                    workDetails.add(workData);
//                }
//                userReport.put("workDetails", workDetails);

                // Event details
                List<Map<String, String>> eventDetails = new ArrayList<>();
                for (Event event : events) {
                    Map<String, String> eventData = new HashMap<>();
                    eventData.put("date", event.getDate().toString());
                    eventData.put("location", event.getLocation());
                    eventData.put("status", event.getStatus());
                    eventData.put("time", event.getTime().toString());  // Added time
                    eventDetails.add(eventData);
                }
                userReport.put("eventDetails", eventDetails);

                // Add the user report to the list
                allUsersReports.add(userReport);
            }
        }

//        long endTime = System.currentTimeMillis(); // End time measurement
//        long duration = endTime - startTime; // Calculate time taken

        // Add time taken to response
//        allUsersReports.add(Collections.singletonMap("generationTime", duration + "ms"));

        return ResponseEntity.ok(allUsersReports);
    }

    @GetMapping(path = "/customers")
    public List<Customer> getAllUsers() {
        return (List<Customer>) customerService.getAllEmployee();
    }
}

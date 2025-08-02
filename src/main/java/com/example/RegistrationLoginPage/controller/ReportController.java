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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            Table workTable = new Table(2);
            workTable.addCell("Date");
            workTable.addCell("Description");
            for (Work work : works) {
                workTable.addCell(work.getDate().toString());
                workTable.addCell(work.getDescription());
            }
            document.add(workTable);

            // Event Table
            document.add(new Paragraph("Event Details:").setFontSize(16).setBold());
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

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-report.pdf")
                .body(byteArrayOutputStream.toByteArray());
    }

    @GetMapping(path = "/customers")
    public List<Customer> getAllUsers() {
        return (List<Customer>) customerService.getAllEmployee();
    }
}

package com.example.RegistrationLoginPage.dto;

import java.time.LocalTime;
import java.util.List;

public class UserReportResponse {
    private String customerName;
    private String role;
    private String description;
    private List<ActivityLog> activities;

    public static class ActivityLog {
        private LocalTime time;
        private String location;
        private String status;

        public ActivityLog(LocalTime time, String location, String status) {
            this.time = time;
            this.location = location;
            this.status = status;
        }

        public LocalTime getTime() { return time; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ActivityLog> getActivities() { return activities; }
    public void setActivities(List<ActivityLog> activities) { this.activities = activities; }
}

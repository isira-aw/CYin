package com.example.RegistrationLoginPage.dto;

public class LoginResponseDTO {
    private String message;
    private boolean status;
    private String role;
    private String token;

    public LoginResponseDTO(String message, boolean status, String role, String token) {
        this.message = message;
        this.status = status;
        this.role = role;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
// Getters and Setters
}

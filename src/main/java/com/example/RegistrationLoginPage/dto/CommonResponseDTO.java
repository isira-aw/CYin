package com.example.RegistrationLoginPage.dto;

import lombok.Data;

@Data
public class CommonResponseDTO {
    private boolean status;
    private String message;
    private Object data;
}

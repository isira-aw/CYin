package com.example.RegistrationLoginPage.dto;

import lombok.Data;

@Data
public class CommonResponseDTO {
    private boolean status;
    private String message;
    private Object data;

    public CommonResponseDTO() {}

    // Constructor that accepts all three parameters
    public CommonResponseDTO(boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}

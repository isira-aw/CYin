package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.CustomerDTO;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/customers")

public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(path = "/signUp")
    public ResponseEntity<CommonResponseDTO> saveEmployee(@RequestBody CustomerDTO customerDTO) {
        String result = customerService.addEmployee(customerDTO);

        CommonResponseDTO response = new CommonResponseDTO();
        if (result.contains("successful")) {
            response.setStatus(true);
            response.setMessage(result);
            response.setData("Registration success");
            return ResponseEntity.ok(response);
        } else {
            response.setStatus(false);
            response.setMessage(result);
            response.setData("Registration failed");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

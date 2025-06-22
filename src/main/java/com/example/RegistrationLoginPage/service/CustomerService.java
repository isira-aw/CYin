package com.example.RegistrationLoginPage.service;

import com.example.RegistrationLoginPage.dto.CustomerDTO;
import com.example.RegistrationLoginPage.response.LoginResponse;

public interface CustomerService {
    String addEmployee(CustomerDTO customerDTO);

    Object getAllEmployee();
}

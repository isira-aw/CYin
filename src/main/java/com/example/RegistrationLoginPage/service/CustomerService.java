package com.example.RegistrationLoginPage.service;

import com.example.RegistrationLoginPage.dto.CustomerDTO;

public interface CustomerService {
    String addEmployee(CustomerDTO customerDTO);
    Object getAllEmployee();
}

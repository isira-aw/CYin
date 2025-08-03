package com.example.RegistrationLoginPage.service.implement;

import com.example.RegistrationLoginPage.dto.CustomerDTO;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerImplements implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String addEmployee(CustomerDTO customerDTO) {
        Optional<Customer> existingCustomer = customerRepository.findByEmail(customerDTO.getEmail());

        if (existingCustomer.isPresent()) {
            return "Email already registered. Please log in instead.";
        }

        Customer customer = new Customer(
                customerDTO.getEmployeeId(),
                customerDTO.getEmployeeName(),
                customerDTO.getEmail(),
                customerDTO.getRole(),
                this.passwordEncoder.encode(customerDTO.getPassword())
        );

        customerRepository.save(customer);
        return "Registration successful for: " + customer.getCustomerName();
    }

//    @Override
//    public List<Customer> getAllEmployee() {
//        return customerRepository.findAll();
//    }

    @Override
    public List<Customer> getAllEmployee() {
        // Fetch all customers and filter out the ones with the role "ADMIN"
        return customerRepository.findAll().stream()
                .filter(customer -> !customer.getRole().equals("ADMIN"))
                .collect(Collectors.toList());
    }
}

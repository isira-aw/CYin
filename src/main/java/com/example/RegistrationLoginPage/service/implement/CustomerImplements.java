package com.example.RegistrationLoginPage.service.implement;

import com.example.RegistrationLoginPage.dto.CustomerDTO;
import com.example.RegistrationLoginPage.dto.LoginDTO;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.response.LoginResponse;
import com.example.RegistrationLoginPage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Override
    public LoginResponse loginEmployee(LoginDTO loginDTO) {
        Optional<Customer> customerOptional = customerRepository.findByEmail(loginDTO.getEmail());

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();

            String rawPassword = loginDTO.getPassword();
            String encodedPassword = customer.getPassword();
            String role = customer.getRole();

            boolean isPasswordCorrect = passwordEncoder.matches(rawPassword, encodedPassword);

            if (isPasswordCorrect) {
                Optional<Customer> employee = customerRepository.findOneByEmailAndPassword(loginDTO.getEmail(), encodedPassword);
                if (employee.isPresent()) {
                    return new LoginResponse("Login Successfully", true, role);
                } else {
                    return new LoginResponse("Login failed after password match", false, null);
                }
            } else {
                return new LoginResponse("Incorrect password", false, null);
            }
        } else {
            return new LoginResponse("Email not found", false, null);
        }
    }

    @Override
    public List<Customer> getAllEmployee() {
        return customerRepository.findAll();
    }
}

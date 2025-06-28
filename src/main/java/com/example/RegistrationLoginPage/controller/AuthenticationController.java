package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.config.JwtTokenProvider;
import com.example.RegistrationLoginPage.dto.CustomerDTO;
import com.example.RegistrationLoginPage.dto.LoginRequestDTO;
import com.example.RegistrationLoginPage.dto.LoginResponseDTO;
import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(
                new LoginResponseDTO("Login Successful", true, "USER", jwt)
        );
    }
    @Autowired
    private CustomerService customerService;

    @PostMapping(path = "/signUp")
    public String saveEmployee(@RequestBody CustomerDTO customerDTO) {
        return customerService.addEmployee(customerDTO);
    }

    @Component
    public class DataInitializer implements ApplicationRunner {

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(ApplicationArguments args) {
            if (customerRepository.findByCustomerName("superadmin").isEmpty()) {
                Customer superAdmin = new Customer();
                superAdmin.setCustomerName("superadmin");
                superAdmin.setEmail("superadmin@example.com");
                superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
                superAdmin.setRole("ROLE_SUPER_ADMIN");

                customerRepository.save(superAdmin);
                System.out.println("Super Admin user created.");
            } else {
                System.out.println("Super Admin already exists.");
            }
        }
    }

}

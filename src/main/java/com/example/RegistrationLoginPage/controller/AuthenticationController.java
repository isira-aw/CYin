package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.entity.Customer;
import com.example.RegistrationLoginPage.repository.CustomerRepository;
import com.example.RegistrationLoginPage.security.JwtTokenProvider;
import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.LoginRequestDTO;
import com.example.RegistrationLoginPage.dto.LoginResponseDTO;
import com.example.RegistrationLoginPage.service.EmailService;
import com.example.RegistrationLoginPage.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<CommonResponseDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        Optional<Customer> custormer = customerRepository.findByEmail(loginRequest.getEmail());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        LoginResponseDTO loginResponse = new LoginResponseDTO("Login Successful", true, custormer.get().getRole(), jwt);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Login successful");
        response.setData(loginResponse);

        return ResponseEntity.ok(response);
    }
    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponseDTO>  forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = passwordResetService.createResetToken(email);
        emailService.sendResetLink(email, token);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Forgot password successfully");
        response.setData(email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<CommonResponseDTO>  resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        passwordResetService.resetPassword(token, newPassword);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Password reset successful");
        response.setData(newPassword);

        return ResponseEntity.ok(response);
    }
}
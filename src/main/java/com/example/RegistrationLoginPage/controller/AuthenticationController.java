package com.example.RegistrationLoginPage.controller;

import com.example.RegistrationLoginPage.security.JwtTokenProvider;
import com.example.RegistrationLoginPage.dto.CommonResponseDTO;
import com.example.RegistrationLoginPage.dto.LoginRequestDTO;
import com.example.RegistrationLoginPage.dto.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

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

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        LoginResponseDTO loginResponse = new LoginResponseDTO("Login Successful", true, "USER", jwt);

        CommonResponseDTO response = new CommonResponseDTO();
        response.setStatus(true);
        response.setMessage("Login successful");
        response.setData(loginResponse);

        return ResponseEntity.ok(response);
    }
}

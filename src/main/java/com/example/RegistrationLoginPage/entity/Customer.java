package com.example.RegistrationLoginPage.entity;

import javax.persistence.*;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(name = "id", length = 50)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "password", length = 255)
    private String password;

    // Constructor: Fix parameter name from 'Id' to 'id'
    public Customer(int id, String customerName, String email, String role, String password) {
        this.id = id; // Use lowercase 'id'
        this.customerName = customerName;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    // Default constructor
    public Customer() {
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

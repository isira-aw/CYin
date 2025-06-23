package com.example.RegistrationLoginPage.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Customer customer;

    private String description;

    private LocalDate date;


    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }


    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

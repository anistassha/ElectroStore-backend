package com.bsuir.ElectroStore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;
    private String firstName;
    private String lastName;
    private String gender;
    private String phoneNumber;
    private String email;

    public Customer() {}

    public Customer(int id) {
        this.customerId = id;
    }

    public Customer(int id, String firstName, String lastName, String gender, String phoneNumber, String email) {
        this.customerId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}


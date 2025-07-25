package com.bsuir.ElectroStore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;

    @NotBlank(message = "Имя не должно быть пустым")
    private String firstName;

    @NotBlank(message = "Фамилия не должна быть пустой")
    private String lastName;

    @NotBlank(message = "Пол не должен быть пустым")
    private String gender;

    @Pattern(regexp = "\\+375\\d{9}", message = "Телефон должен быть в формате +375XXXXXXXXX")
    private String phoneNumber;

    @Email(message = "Некорректный email")
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


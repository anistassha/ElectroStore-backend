package com.bsuir.ElectroStore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;
    private String status;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeData employee;
}
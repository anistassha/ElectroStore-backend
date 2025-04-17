package com.bsuir.ElectroStore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmployeeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private int employeeId;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone_number")
    private String telephone_number;
}

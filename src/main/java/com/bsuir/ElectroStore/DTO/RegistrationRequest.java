package com.bsuir.ElectroStore.DTO;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String username;
    private String password;
    private String name;
    private String surname;
    private String email;
    private String telephoneNumber;
}
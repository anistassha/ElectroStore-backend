package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.DTO.RegistrationRequest;
import com.bsuir.ElectroStore.model.AppUser;
import com.bsuir.ElectroStore.model.EmployeeData;
import com.bsuir.ElectroStore.repository.EmployeeRepository;
import com.bsuir.ElectroStore.repository.UserRepository;
import com.bsuir.ElectroStore.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Ошибка: Имя пользователя уже занято");
        }

        if (employeeRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Ошибка: Email уже используется");
        }

        EmployeeData employee = new EmployeeData();
        employee.setName(request.getName());
        employee.setSurname(request.getSurname());
        employee.setEmail(request.getEmail());
        employee.setTelephone_number(request.getTelephoneNumber());
        employeeRepository.save(employee);

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("EMPLOYEE");
        user.setStatus("ACTIVE");
        user.setEmployee(employee);
        userRepository.save(user);

        return ResponseEntity.ok("Успешная регистрация!");
    }

    @PostMapping("/login")
    public String login(@RequestBody AppUser request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}

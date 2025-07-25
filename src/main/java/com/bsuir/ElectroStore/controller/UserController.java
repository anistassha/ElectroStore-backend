package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.model.AppUser;
import com.bsuir.ElectroStore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{userId}/toggle-role")
    public ResponseEntity<AppUser> toggleUserRole(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    String newRole = user.getRole().equals("ADMIN") ? "EMPLOYEE" : "ADMIN";
                    user.setRole(newRole);
                    AppUser updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
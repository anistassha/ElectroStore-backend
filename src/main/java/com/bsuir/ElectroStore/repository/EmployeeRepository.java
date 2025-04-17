package com.bsuir.ElectroStore.repository;

import com.bsuir.ElectroStore.model.EmployeeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeData, Integer> {
    boolean existsByEmail(String email);
}

package com.bsuir.ElectroStore.repository;

import com.bsuir.ElectroStore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    List<Customer> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
}

package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.model.Customer;
import com.bsuir.ElectroStore.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
@CrossOrigin("http://localhost:3000")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Получить всех покупателей
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }

    // Получить покупателя по ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable int id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Создать нового покупателя
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    // Обновить покупателя
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable int id, @RequestBody Customer customerDetails) {

        return customerRepository.findById(id).map(existingCustomer -> {
                    existingCustomer.setFirstName(customerDetails.getFirstName());
                    existingCustomer.setLastName(customerDetails.getLastName());
                    existingCustomer.setPhoneNumber(customerDetails.getPhoneNumber());
                    existingCustomer.setEmail(customerDetails.getEmail());
                    existingCustomer.setGender(customerDetails.getGender());
                    Customer updatedCustomer = customerRepository.save(existingCustomer);
                    return ResponseEntity.ok(updatedCustomer);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Удалить покупателя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable int id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Поиск покупателей по имени или фамилии
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String query) {
        List<Customer> customers = customerRepository.findByFirstNameContainingOrLastNameContaining(query, query);
        return ResponseEntity.ok(customers);
    }
}
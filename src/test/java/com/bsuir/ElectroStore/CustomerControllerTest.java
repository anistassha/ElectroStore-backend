package com.bsuir.ElectroStore;

import com.bsuir.ElectroStore.controller.CustomerController;
import com.bsuir.ElectroStore.model.Customer;
import com.bsuir.ElectroStore.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerController customerController;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(1, "Иван", "Иванов", "Мужской", "+375291234567", "ivan@example.com");
    }

    @Test
    void getAllCustomers_ShouldReturnListOfCustomers() {
        // Arrange
        List<Customer> customers = Arrays.asList(customer, new Customer(2, "Мария", "Петрова", "Женский", "+375297654321", "maria@example.com"));
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        ResponseEntity<List<Customer>> response = customerController.getAllCustomers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Иван", response.getBody().get(0).getFirstName());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomer() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        // Act
        ResponseEntity<Customer> response = customerController.getCustomerById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customer, response.getBody());
        verify(customerRepository, times(1)).findById(1);
    }

    @Test
    void getCustomerById_WhenCustomerNotFound_ShouldReturnNotFound() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Customer> response = customerController.getCustomerById(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerRepository, times(1)).findById(1);
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer() {
        // Arrange
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        ResponseEntity<Customer> response = customerController.createCustomer(customer);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(customer, response.getBody());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void updateCustomer_WhenCustomerExists_ShouldReturnUpdatedCustomer() {
        // Arrange
        Customer updatedDetails = new Customer(1, "Петр", "Сидоров", "Мужской", "+375293214567", "petr@example.com");
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedDetails);

        // Act
        ResponseEntity<Customer> response = customerController.updateCustomer(1, updatedDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Петр", response.getBody().getFirstName());
        assertEquals("Сидоров", response.getBody().getLastName());
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenCustomerNotFound_ShouldReturnNotFound() {
        // Arrange
        Customer updatedDetails = new Customer(1, "Петр", "Сидоров", "Мужской", "+375293214567", "petr@example.com");
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Customer> response = customerController.updateCustomer(1, updatedDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerRepository, times(1)).findById(1);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldReturnNoContent() {
        // Arrange
        when(customerRepository.existsById(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = customerController.deleteCustomer(1);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerRepository, times(1)).existsById(1);
        verify(customerRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteCustomer_WhenCustomerNotFound_ShouldReturnNotFound() {
        // Arrange
        when(customerRepository.existsById(1)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = customerController.deleteCustomer(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerRepository, times(1)).existsById(1);
        verify(customerRepository, never()).deleteById(anyInt());
    }

    @Test
    void searchCustomers_ShouldReturnMatchingCustomers() {
        // Arrange
        List<Customer> customers = Arrays.asList(customer, new Customer(2, "Иван", "Петров", "Мужской", "+375297654321", "ivan.p@example.com"));
        when(customerRepository.findByFirstNameContainingOrLastNameContaining("Иван", "Иван")).thenReturn(customers);

        // Act
        ResponseEntity<List<Customer>> response = customerController.searchCustomers("Иван");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Иван", response.getBody().get(0).getFirstName());
        verify(customerRepository, times(1)).findByFirstNameContainingOrLastNameContaining("Иван", "Иван");
    }
}
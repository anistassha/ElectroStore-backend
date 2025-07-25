package com.bsuir.ElectroStore.repository;

import com.bsuir.ElectroStore.model.AppUser;
import com.bsuir.ElectroStore.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByUserId(Long userId);
    List<Purchase> findByDateBetween(String startDate, String endDate);
}

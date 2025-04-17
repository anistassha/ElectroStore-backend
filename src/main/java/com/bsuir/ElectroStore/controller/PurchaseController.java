package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.model.Purchase;
import com.bsuir.ElectroStore.model.Product;
import com.bsuir.ElectroStore.repository.PurchaseRepository;
import com.bsuir.ElectroStore.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseController(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    // Получить все покупки
    @GetMapping
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        return ResponseEntity.ok(purchaseRepository.findAll());
    }

    // Получить покупку по ID
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable int id) {
        return purchaseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Добавить новую покупку с уменьшением количества товара
    @PostMapping
    public ResponseEntity<?> createPurchase(@RequestBody Purchase purchase) {
        Product product = productRepository.findById(purchase.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверка наличия товара на складе
        if (product.getStockQuantity() < purchase.getQuantity()) {
            return ResponseEntity.badRequest().body("Недостаточно товара на складе");
        }

        // Уменьшаем количество на складе
        product.setStockQuantity(product.getStockQuantity() - purchase.getQuantity());
        productRepository.save(product);

        // Сохраняем покупку
        Purchase savedPurchase = purchaseRepository.save(purchase);
        return ResponseEntity.ok(savedPurchase);
    }

    // Обновить покупку
    @PutMapping("/{id}")
    public ResponseEntity<Purchase> updatePurchase(@PathVariable int id, @RequestBody Purchase updatedPurchase) {
        return purchaseRepository.findById(id)
                .map(purchase -> {
                    purchase.setProduct(updatedPurchase.getProduct());
                    purchase.setCustomer(updatedPurchase.getCustomer());
                    purchase.setUser(updatedPurchase.getUser());
                    purchase.setDate(updatedPurchase.getDate());
                    purchase.setQuantity(updatedPurchase.getQuantity());
                    purchase.setPayAmount(updatedPurchase.getPayAmount());
                    return ResponseEntity.ok(purchaseRepository.save(purchase));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить покупку
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable int id) {
        if (purchaseRepository.existsById(id)) {
            purchaseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

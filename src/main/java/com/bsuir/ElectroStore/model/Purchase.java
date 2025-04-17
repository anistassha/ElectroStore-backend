package com.bsuir.ElectroStore.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchaseId")
    private int purchaseId;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private AppUser user;

    @Column(name = "date")
    private String date;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "payAmount", nullable = false)
    private double payAmount;

    public Purchase(int purchaseId, Product product, Customer customer, AppUser user, String date, int quantity, double payAmount) {
        this.purchaseId = purchaseId;
        this.product = product;
        this.customer = customer;
        this.user = user;
        this.date = date;
        this.quantity = quantity;
        this.payAmount = payAmount;
    }

    public Purchase() {}
}

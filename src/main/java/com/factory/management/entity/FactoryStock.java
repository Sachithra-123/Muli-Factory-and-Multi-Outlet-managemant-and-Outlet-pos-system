package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "factory_stock", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"factory_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(name = "low_stock_threshold")
    private int lowStockThreshold = 10;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }
}

package com.factory.management.service;

import com.factory.management.entity.FactoryStock;
import com.factory.management.repository.FactoryStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FactoryStockService {

    private final FactoryStockRepository factoryStockRepository;

    public FactoryStockService(FactoryStockRepository factoryStockRepository) {
        this.factoryStockRepository = factoryStockRepository;
    }

    public List<FactoryStock> findAll() {
        return factoryStockRepository.findAll();
    }

    public FactoryStock findByProductId(Long productId) {
        return factoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found for product: " + productId));
    }

    public FactoryStock findById(Long id) {
        return factoryStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock record not found: " + id));
    }

    public FactoryStock adjustStock(Long stockId, int newQuantity) {
        FactoryStock stock = findById(stockId);
        stock.setQuantity(newQuantity);
        stock.setUpdatedAt(LocalDateTime.now());
        return factoryStockRepository.save(stock);
    }

    public FactoryStock addStock(Long id, int addQty) {
        FactoryStock stock = findById(id);
        stock.setQuantity(stock.getQuantity() + addQty);
        stock.setUpdatedAt(LocalDateTime.now());
        return factoryStockRepository.save(stock);
    }

    public FactoryStock updateThreshold(Long stockId, int threshold) {
        FactoryStock stock = findById(stockId);
        stock.setLowStockThreshold(threshold);
        return factoryStockRepository.save(stock);
    }

    public List<FactoryStock> getLowStockItems() {
        return factoryStockRepository.findLowStockItems();
    }

    public long countLowStock() {
        return factoryStockRepository.findLowStockItems().size();
    }
}

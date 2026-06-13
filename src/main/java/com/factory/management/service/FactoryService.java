package com.factory.management.service;

import com.factory.management.entity.Factory;
import com.factory.management.entity.FactoryStock;
import com.factory.management.entity.Product;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.FactoryStockRepository;
import com.factory.management.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FactoryService {

    private final FactoryRepository factoryRepository;
    private final FactoryStockRepository factoryStockRepository;
    private final ProductRepository productRepository;

    public FactoryService(FactoryRepository factoryRepository, 
                          FactoryStockRepository factoryStockRepository,
                          ProductRepository productRepository) {
        this.factoryRepository = factoryRepository;
        this.factoryStockRepository = factoryStockRepository;
        this.productRepository = productRepository;
    }

    public List<Factory> findAll() {
        return factoryRepository.findAll();
    }

    public Factory findById(Long id) {
        return factoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factory not found: " + id));
    }

    public Factory save(Factory factory) {
        Factory saved = factoryRepository.save(factory);
        // Auto-create factory stock entry for new factory for all active products
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            FactoryStock stock = new FactoryStock();
            stock.setFactory(saved);
            stock.setProduct(product);
            stock.setQuantity(0);
            stock.setLowStockThreshold(10);
            factoryStockRepository.save(stock);
        }
        return saved;
    }

    public Factory update(Long id, Factory updated) {
        Factory existing = findById(id);
        existing.setName(updated.getName());
        existing.setLocation(updated.getLocation());
        existing.setPhone(updated.getPhone());
        return factoryRepository.save(existing);
    }

    public void delete(Long id) {
        factoryRepository.deleteById(id);
    }

    public List<FactoryStock> getFactoryStock(Long factoryId) {
        return factoryStockRepository.findByFactoryId(factoryId);
    }

    public long count() {
        return factoryRepository.count();
    }
}

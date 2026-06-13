package com.factory.management.service;

import com.factory.management.entity.Factory;
import com.factory.management.entity.FactoryStock;
import com.factory.management.entity.Product;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.FactoryStockRepository;
import com.factory.management.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final FactoryStockRepository factoryStockRepository;
    private final FactoryRepository factoryRepository;

    public ProductService(ProductRepository productRepository, 
                          FactoryStockRepository factoryStockRepository,
                          FactoryRepository factoryRepository) {
        this.productRepository = productRepository;
        this.factoryStockRepository = factoryStockRepository;
        this.factoryRepository = factoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public Product save(Product product) {
        Product saved = productRepository.save(product);
        // Auto-create factory stock entry for new product for each active factory
        List<Factory> factories = factoryRepository.findAll();
        for (Factory factory : factories) {
            if (factoryStockRepository.findByFactoryIdAndProductId(factory.getId(), saved.getId()).isEmpty()) {
                FactoryStock stock = new FactoryStock();
                stock.setFactory(factory);
                stock.setProduct(saved);
                stock.setQuantity(0);
                stock.setLowStockThreshold(10);
                factoryStockRepository.save(stock);
            }
        }
        return saved;
    }

    public Product update(Long id, Product updated) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return productRepository.searchProducts(query.trim());
    }

    public long count() {
        return productRepository.count();
    }
}

package com.factory.management.controller;

import com.factory.management.entity.Product;
import com.factory.management.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q) {
        List<Product> products = productService.search(q);
        return products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("category", p.getCategory());
            map.put("unitPrice", p.getUnitPrice());
            map.put("imageUrl", p.getImageUrl());
            int totalStock = p.getFactoryStocks() != null ? p.getFactoryStocks().stream().mapToInt(com.factory.management.entity.FactoryStock::getQuantity).sum() : 0;
            map.put("stock", totalStock);
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Product p = productService.findById(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("category", p.getCategory());
        map.put("unitPrice", p.getUnitPrice());
        return map;
    }
}

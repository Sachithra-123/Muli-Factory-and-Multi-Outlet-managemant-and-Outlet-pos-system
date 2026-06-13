package com.factory.management.service;

import com.factory.management.dto.CartItemDTO;
import com.factory.management.dto.SaleRequestDTO;
import com.factory.management.entity.*;
import com.factory.management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final OutletStockRepository outletStockRepository;
    private final ProductRepository productRepository;
    private final OutletRepository outletRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository, SaleItemRepository saleItemRepository,
                       OutletStockRepository outletStockRepository, ProductRepository productRepository,
                       OutletRepository outletRepository, UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.outletStockRepository = outletStockRepository;
        this.productRepository = productRepository;
        this.outletRepository = outletRepository;
        this.userRepository = userRepository;
    }

    public Sale completeSale(SaleRequestDTO request, String cashierUsername) {
        Outlet outlet = outletRepository.findById(request.getOutletId())
                .orElseThrow(() -> new RuntimeException("Outlet not found"));
        User cashier = userRepository.findByUsername(cashierUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // First, check if the items are actually available
        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDTO item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            OutletStock outletStock = outletStockRepository
                    .findByOutletIdAndProductId(request.getOutletId(), item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not in stock at this outlet: " + product.getName()));

            if (outletStock.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName()
                        + ". Available: " + outletStock.getQuantity());
            }

            // Reduce the stock count in the outlet
            outletStock.setQuantity(outletStock.getQuantity() - item.getQuantity());
            outletStock.setUpdatedAt(LocalDateTime.now());
            outletStockRepository.save(outletStock);

            BigDecimal subtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            SaleItem si = new SaleItem();
            si.setProduct(product);
            si.setQuantity(item.getQuantity());
            si.setUnitPrice(product.getUnitPrice());
            si.setSubtotal(subtotal);
            saleItems.add(si);
        }

        // Record the transaction details
        Sale sale = new Sale();
        sale.setOutlet(outlet);
        sale.setCashier(cashier);
        sale.setTotalAmount(total);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setSaleDate(LocalDateTime.now());
        Sale savedSale = saleRepository.save(sale);

        // Connect the individual items to the main sale record
        for (SaleItem si : saleItems) {
            si.setSale(savedSale);
            saleItemRepository.save(si);
        }

        return savedSale;
    }

    public Sale findById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id));
    }

    public List<Sale> findAll() {
        return saleRepository.findAllByOrderBySaleDateDesc();
    }

    public List<Sale> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return saleRepository.sumTotalAmountBetween(start, end);
    }

    public long getTodaySaleCount() {
        return findByDate(LocalDate.now()).size();
    }
}

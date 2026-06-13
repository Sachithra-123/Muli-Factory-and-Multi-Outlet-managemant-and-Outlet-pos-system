package com.factory.management.service;

import com.factory.management.dto.StockTransferDTO;
import com.factory.management.entity.*;
import com.factory.management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final FactoryStockRepository factoryStockRepository;
    private final OutletStockRepository outletStockRepository;
    private final ProductRepository productRepository;
    private final OutletRepository outletRepository;
    private final FactoryRepository factoryRepository;

    public StockTransferService(StockTransferRepository stockTransferRepository,
                                FactoryStockRepository factoryStockRepository,
                                OutletStockRepository outletStockRepository,
                                ProductRepository productRepository,
                                OutletRepository outletRepository,
                                FactoryRepository factoryRepository) {
        this.stockTransferRepository = stockTransferRepository;
        this.factoryStockRepository = factoryStockRepository;
        this.outletStockRepository = outletStockRepository;
        this.productRepository = productRepository;
        this.outletRepository = outletRepository;
        this.factoryRepository = factoryRepository;
    }

    public List<StockTransfer> findAll() {
        return stockTransferRepository.findAllByOrderByTransferDateDesc();
    }

    public StockTransfer transfer(StockTransferDTO dto) {
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Outlet outlet = outletRepository.findById(dto.getOutletId())
                .orElseThrow(() -> new RuntimeException("Outlet not found"));

        // Take the items out of the factory's inventory
        FactoryStock factoryStock = factoryStockRepository.findByFactoryIdAndProductId(dto.getFactoryId(), dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Factory stock not found for this product in the selected factory"));

        if (factoryStock.getQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Insufficient factory stock. Available: " + factoryStock.getQuantity());
        }
        factoryStock.setQuantity(factoryStock.getQuantity() - dto.getQuantity());
        factoryStock.setUpdatedAt(LocalDateTime.now());
        factoryStockRepository.save(factoryStock);

        // Put the items into the outlet's inventory
        OutletStock outletStock = outletStockRepository
                .findByOutletIdAndProductId(dto.getOutletId(), dto.getProductId())
                .orElseGet(() -> {
                    OutletStock os = new OutletStock();
                    os.setOutlet(outlet);
                    os.setProduct(product);
                    os.setQuantity(0);
                    return os;
                });
        outletStock.setQuantity(outletStock.getQuantity() + dto.getQuantity());
        outletStock.setUpdatedAt(LocalDateTime.now());
        outletStockRepository.save(outletStock);

        // Keep a record of the transfer for history and reporting
        StockTransfer transfer = new StockTransfer();
        transfer.setSourceFactory(factory);
        transfer.setProduct(product);
        transfer.setOutlet(outlet);
        transfer.setQuantity(dto.getQuantity());
        transfer.setNote(dto.getNote());
        transfer.setStatus("COMPLETED");
        transfer.setTransferDate(LocalDateTime.now());
        return stockTransferRepository.save(transfer);
    }

    public List<StockTransfer> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return stockTransferRepository.findByTransferDateBetween(start, end);
    }
}

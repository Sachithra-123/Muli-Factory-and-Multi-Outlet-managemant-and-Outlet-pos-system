package com.factory.management.service;

import com.factory.management.entity.Outlet;
import com.factory.management.entity.OutletStock;
import com.factory.management.repository.OutletRepository;
import com.factory.management.repository.OutletStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OutletService {

    private final OutletRepository outletRepository;
    private final OutletStockRepository outletStockRepository;

    public OutletService(OutletRepository outletRepository, OutletStockRepository outletStockRepository) {
        this.outletRepository = outletRepository;
        this.outletStockRepository = outletStockRepository;
    }

    public List<Outlet> findAll() {
        return outletRepository.findAll();
    }

    public Outlet findById(Long id) {
        return outletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Outlet not found: " + id));
    }

    public Outlet save(Outlet outlet) {
        return outletRepository.save(outlet);
    }

    public Outlet update(Long id, Outlet updated) {
        Outlet existing = findById(id);
        existing.setName(updated.getName());
        existing.setLocation(updated.getLocation());
        existing.setPhone(updated.getPhone());
        return outletRepository.save(existing);
    }

    public void delete(Long id) {
        outletRepository.deleteById(id);
    }

    public List<OutletStock> getOutletStock(Long outletId) {
        return outletStockRepository.findByOutletId(outletId);
    }

    public long count() {
        return outletRepository.count();
    }
}

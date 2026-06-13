package com.factory.management.repository;

import com.factory.management.entity.FactoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryStockRepository extends JpaRepository<FactoryStock, Long> {

    Optional<FactoryStock> findByProductId(Long productId);

    Optional<FactoryStock> findByFactoryIdAndProductId(Long factoryId, Long productId);

    List<FactoryStock> findByFactoryId(Long factoryId);

    @Query("SELECT fs FROM FactoryStock fs WHERE fs.quantity <= fs.lowStockThreshold")
    List<FactoryStock> findLowStockItems();

    @Query("SELECT fs FROM FactoryStock fs WHERE fs.factory.id = :factoryId AND fs.quantity <= fs.lowStockThreshold")
    List<FactoryStock> findLowStockItemsByFactoryId(Long factoryId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE factory_stock SET factory_id = :factoryId WHERE factory_id = 0 OR factory_id IS NULL", nativeQuery = true)
    void migrateInvalidFactoryStocks(Long factoryId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "DELETE FROM factory_stock WHERE factory_id = 0 OR factory_id IS NULL", nativeQuery = true)
    void deleteInvalidFactoryStocks();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "ALTER TABLE factory_stock DROP INDEX UK_3nx32dg3r6a3crpl0qjjnu24j", nativeQuery = true)
    void dropOldUniqueConstraint();
}

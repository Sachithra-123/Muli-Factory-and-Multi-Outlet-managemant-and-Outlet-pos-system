package com.factory.management.repository;

import com.factory.management.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findByOutletId(Long outletId);

    List<StockTransfer> findByTransferDateBetween(LocalDateTime start, LocalDateTime end);

    List<StockTransfer> findAllByOrderByTransferDateDesc();
}

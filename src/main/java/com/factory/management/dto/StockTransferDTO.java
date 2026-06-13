package com.factory.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDTO {
    private Long factoryId;
    private Long productId;
    private Long outletId;
    private int quantity;
    private String note;
}

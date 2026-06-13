package com.factory.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequestDTO {
    private Long outletId;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private List<CartItemDTO> items;
}

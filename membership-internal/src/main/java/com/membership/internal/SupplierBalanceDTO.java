package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierBalanceDTO {
    private Long id;
    private Long supplierId;
    private BigDecimal totalRecharge;
    private BigDecimal consumedAmount;
    private BigDecimal remainingAmount;
    private BigDecimal alertThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联信息，用于前端显示
    private String supplierName;
    private String supplierCode;
}
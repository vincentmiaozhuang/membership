package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierProductDTO {
    private Long id;
    private Long productId;
    private Long supplierId;
    private BigDecimal supplierPrice;
    private Integer stockQuantity;
    private Integer salesQuantity;
    private BigDecimal stockAmount;
    private BigDecimal salesAmount;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联信息，用于前端显示
    private String productName;
    private String productCode;
    private String productType;
    private BigDecimal productFaceValue;
    private String supplierName;
    private String supplierCode;
}
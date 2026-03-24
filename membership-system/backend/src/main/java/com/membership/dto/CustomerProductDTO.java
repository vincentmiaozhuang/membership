package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerProductDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private String productCode;
    private String productType;
    private BigDecimal faceValue;
    private String customerProductCode;
    private BigDecimal customerPrice;
    private Integer stockQuantity;
    private Integer shipmentQuantity;
    private BigDecimal stockAmount;
    private BigDecimal shipmentAmount;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecordDTO {
    private Long id;
    private Long customerProductId;
    private Long productId;
    private String productName;
    private String rechargePhone;
    private String customerOrderId;
    private String platformOrderId;
    private Long customerId;
    private String customerName;
    private BigDecimal customerPrice;
    private Long supplierId;
    private String supplierName;
    private BigDecimal supplierPrice;
    private BigDecimal productFacePrice;
    private String status;
    private String description;
    private Long rechargePerson;
    private LocalDateTime createdAt;
}

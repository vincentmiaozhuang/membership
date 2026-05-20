package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
public class RechargeStatsDTO {
    private Long id;
    private Date statDate;
    private Long productId;
    private String productName;
    private BigDecimal productFacePrice;
    private Long customerId;
    private String customerName;
    private Long supplierId;
    private String supplierName;
    private String status;
    private Integer rechargeCount;
    private BigDecimal customerPrice;
    private BigDecimal customerAmount;
    private BigDecimal supplierPrice;
    private BigDecimal costAmount;
}

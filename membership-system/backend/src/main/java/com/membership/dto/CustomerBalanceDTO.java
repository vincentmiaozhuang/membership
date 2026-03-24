package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerBalanceDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal paymentAccountTotal;
    private BigDecimal paymentAccountConsumed;
    private BigDecimal paymentAccountBalance;
    private BigDecimal creditAccountTotal;
    private BigDecimal creditAccountConsumed;
    private BigDecimal creditAccountBalance;
    private BigDecimal totalAccountAmount;
    private BigDecimal totalAccountConsumed;
    private BigDecimal totalAccountBalance;
    private BigDecimal alertThreshold;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

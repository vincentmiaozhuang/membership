package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerPaymentDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal paymentAmount;
    private BigDecimal creditAmount;
    private String financialScreenshot;
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

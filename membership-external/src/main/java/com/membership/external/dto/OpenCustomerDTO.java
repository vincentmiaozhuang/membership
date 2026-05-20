package com.membership.external.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OpenCustomerDTO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private BigDecimal paymentTotal;
    private BigDecimal creditTotal;
    private BigDecimal paymentBalance;
    private BigDecimal creditBalance;
    private LocalDateTime createdAt;
}
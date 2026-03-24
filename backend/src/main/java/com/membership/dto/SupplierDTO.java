package com.membership.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDTO {
    private Long id;
    private String name;
    private String supplierCode;
    private LocalDateTime cooperationStartDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean enabled;
}

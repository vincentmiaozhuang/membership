package com.membership.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String productCode;
    private String type;
    private BigDecimal faceValue;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
}

package com.membership.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_products")
public class CustomerProduct extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false, unique = true, length = 32)
    private String customerProductCode;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal customerPrice;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false)
    private Integer shipmentQuantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal stockAmount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shipmentAmount;
    
    @Column(nullable = false)
    private Boolean enabled = true;
}
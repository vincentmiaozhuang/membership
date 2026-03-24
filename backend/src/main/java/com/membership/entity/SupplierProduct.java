package com.membership.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "supplier_products")
public class SupplierProduct extends BaseEntity {
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @Column(name = "supplier_price", nullable = false)
    private BigDecimal supplierPrice;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(name = "sales_quantity", nullable = false)
    private Integer salesQuantity;
    
    @Column(name = "stock_amount", nullable = false)
    private BigDecimal stockAmount;
    
    @Column(name = "sales_amount", nullable = false)
    private BigDecimal salesAmount;
    
    @Column(nullable = false)
    private Boolean enabled = true;
}
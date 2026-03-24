package com.membership.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "supplier_recharges")
public class SupplierRecharge extends BaseEntity {
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "screenshot_url")
    private String screenshotUrl;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "operator_name", nullable = false)
    private String operatorName;
    
    @Column(nullable = false)
    private Boolean enabled = true;
}
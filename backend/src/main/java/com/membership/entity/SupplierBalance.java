package com.membership.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "supplier_balances")
public class SupplierBalance extends BaseEntity {
    
    @Column(name = "supplier_id", nullable = false, unique = true)
    private Long supplierId;
    
    @Column(name = "total_recharge", nullable = false)
    private BigDecimal totalRecharge = BigDecimal.ZERO;
    
    @Column(name = "consumed_amount", nullable = false)
    private BigDecimal consumedAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", nullable = false)
    private BigDecimal remainingAmount = BigDecimal.ZERO;
    
    @Column(name = "alert_threshold")
    private BigDecimal alertThreshold;
}
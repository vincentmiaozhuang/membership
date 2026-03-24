package com.membership.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_balances")
public class CustomerBalance extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "payment_account_total", precision = 10, scale = 2)
    private BigDecimal paymentAccountTotal = BigDecimal.ZERO;
    
    @Column(name = "payment_account_consumed", precision = 10, scale = 2)
    private BigDecimal paymentAccountConsumed = BigDecimal.ZERO;
    
    @Column(name = "payment_account_balance", precision = 10, scale = 2)
    private BigDecimal paymentAccountBalance = BigDecimal.ZERO;
    
    @Column(name = "credit_account_total", precision = 10, scale = 2)
    private BigDecimal creditAccountTotal = BigDecimal.ZERO;
    
    @Column(name = "credit_account_consumed", precision = 10, scale = 2)
    private BigDecimal creditAccountConsumed = BigDecimal.ZERO;
    
    @Column(name = "credit_account_balance", precision = 10, scale = 2)
    private BigDecimal creditAccountBalance = BigDecimal.ZERO;
    
    @Column(name = "total_account_amount", precision = 10, scale = 2)
    private BigDecimal totalAccountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_account_consumed", precision = 10, scale = 2)
    private BigDecimal totalAccountConsumed = BigDecimal.ZERO;
    
    @Column(name = "total_account_balance", precision = 10, scale = 2)
    private BigDecimal totalAccountBalance = BigDecimal.ZERO;
    
    @Column(name = "alert_threshold", precision = 10, scale = 2)
    private BigDecimal alertThreshold = BigDecimal.ZERO;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
}

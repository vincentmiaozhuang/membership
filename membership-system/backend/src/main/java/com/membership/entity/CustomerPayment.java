package com.membership.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_payments")
public class CustomerPayment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal creditAmount;
    
    @Column(length = 500)
    private String financialScreenshot;
    
    @Column(nullable = false, length = 50)
    private String operator;
}

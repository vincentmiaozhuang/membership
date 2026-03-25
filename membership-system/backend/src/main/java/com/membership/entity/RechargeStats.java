package com.membership.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "recharge_stats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dimension_status", columnNames = {"stat_date", "product_id", "customer_id", "supplier_id", "status"})
})
@org.hibernate.annotations.Table(appliesTo = "recharge_stats", indexes = {
        @org.hibernate.annotations.Index(name = "idx_stat_date", columnNames = {"stat_date"}),
        @org.hibernate.annotations.Index(name = "idx_product", columnNames = {"product_id", "stat_date"}),
        @org.hibernate.annotations.Index(name = "idx_customer", columnNames = {"customer_id", "stat_date"}),
        @org.hibernate.annotations.Index(name = "idx_supplier", columnNames = {"supplier_id", "stat_date"}),
        @org.hibernate.annotations.Index(name = "idx_status", columnNames = {"status", "stat_date"})
})
public class RechargeStats extends BaseEntity {

    @Column(name = "stat_date", nullable = false)
    private Date statDate;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "recharge_count", columnDefinition = "INT DEFAULT 0")
    private Integer rechargeCount;

    @Column(name = "customer_price", nullable = false)
    private BigDecimal customerPrice;

    @Column(name = "customer_amount", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal customerAmount;

    @Column(name = "supplier_price", nullable = false)
    private BigDecimal supplierPrice;

    @Column(name = "cost_amount", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal costAmount;

    @Column(name = "product_face_price")
    private BigDecimal productFacePrice;

}
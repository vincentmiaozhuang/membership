package com.membership.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "recharge_records")
public class RechargeRecord extends BaseEntity {
    
    @Column(name = "customer_product_id")
    private Long customerProductId;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name", length = 100)
    private String productName;
    
    @Column(name = "recharge_phone", length = 20, nullable = false)
    private String rechargePhone;
    
    @Column(name = "customer_order_id", length = 50)
    private String customerOrderId;
    
    @Column(name = "platform_order_id", length = 50)
    private String platformOrderId;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Column(name = "customer_price", precision = 10, scale = 2)
    private java.math.BigDecimal customerPrice;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @Column(name = "supplier_name", length = 100)
    private String supplierName;
    
    @Column(name = "supplier_price", precision = 10, scale = 2)
    private java.math.BigDecimal supplierPrice;
    
    @Column(name = "product_face_price", precision = 10, scale = 2)
    private java.math.BigDecimal productFacePrice;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "recharge_person", length = 50)
    private String rechargePerson;
}

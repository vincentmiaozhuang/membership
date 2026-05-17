package com.membership.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_product_supplier_orders")
public class CustomerProductSupplierOrder extends BaseEntity {
    
    @Column(name = "customer_product_id", nullable = false)
    private Long customerProductId;
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}

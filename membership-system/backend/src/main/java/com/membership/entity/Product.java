package com.membership.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true, length = 16)
    private String productCode;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private BigDecimal faceValue;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @OneToMany(mappedBy = "product")
    private List<CustomerProduct> customerProducts = new ArrayList<>();
}

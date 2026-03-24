package com.membership.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true, length = 16)
    private String customerCode;
    
    @Column(nullable = false, length = 16)
    private String customerSecret;
    
    @Column
    private String ipWhitelist;
    
    @Column
    private LocalDateTime cooperationStartDate;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @OneToMany(mappedBy = "customer")
    private List<CustomerProduct> customerProducts = new ArrayList<>();
}

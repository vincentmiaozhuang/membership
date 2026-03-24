package com.membership.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true, length = 16)
    private String supplierCode;
    
    @Column
    private LocalDateTime cooperationStartDate;
    
    @Column(nullable = false)
    private Boolean enabled = true;
}

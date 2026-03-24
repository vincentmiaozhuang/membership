package com.membership.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(nullable = false)
    private String action;
    
    @Column
    private String description;
}

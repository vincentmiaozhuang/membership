package com.membership.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Set<Long> permissionIds;
}

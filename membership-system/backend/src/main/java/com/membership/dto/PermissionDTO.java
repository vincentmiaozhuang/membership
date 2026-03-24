package com.membership.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private String name;
    private String resource;
    private String action;
    private String description;
}

package com.membership.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String customerCode;
    private String customerSecret;
    private String ipWhitelist;
    private LocalDateTime cooperationStartDate;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

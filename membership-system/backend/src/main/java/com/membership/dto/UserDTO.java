package com.membership.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String realName;
    private String phone;
    private Boolean enabled;
    private Set<String> roles;
}

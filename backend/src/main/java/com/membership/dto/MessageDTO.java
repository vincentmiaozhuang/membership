package com.membership.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private Boolean isRead;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
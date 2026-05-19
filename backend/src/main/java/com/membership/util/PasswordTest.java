package com.membership.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Encoded password for 'admin123': " + encodedPassword);
        System.out.println("Verify: " + encoder.matches(rawPassword, encodedPassword));
        
        // Test with existing hash from database
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq";
        System.out.println("DB hash matches 'admin123': " + encoder.matches(rawPassword, dbHash));
    }
}

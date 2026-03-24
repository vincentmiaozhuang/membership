package com.membership.controller;

import com.membership.dto.UserDTO;
import com.membership.entity.Role;
import com.membership.entity.User;
import com.membership.repository.RoleRepository;
import com.membership.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(convertToDTO(user));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Email is already in use!"));
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEnabled(userDTO.getEnabled());
        
        Set<Role> roles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(savedUser));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEnabled(userDTO.getEnabled());
        
        if (userDTO.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("User deleted successfully"));
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Password reset successfully"));
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setEnabled(user.getEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}

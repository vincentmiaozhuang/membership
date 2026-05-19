package com.membership.controller;

import com.membership.dto.UserDTO;
import com.membership.entity.Role;
import com.membership.entity.User;
import com.membership.repository.RoleRepository;
import com.membership.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("获取所有用户列表");
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取用户列表，共{}条", userDTOs.size());
        return ResponseEntity.ok(userDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("获取用户详情，ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("用户不存在，ID: {}", id);
                    return new RuntimeException("User not found");
                });
        logger.info("成功获取用户，ID: {}, 用户名: {}", id, user.getUsername());
        return ResponseEntity.ok(convertToDTO(user));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        logger.info("创建用户，用户名: {}, 邮箱: {}", userDTO.getUsername(), userDTO.getEmail());
        
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            logger.warn("用户名已被使用: {}", userDTO.getUsername());
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("邮箱已被使用: {}", userDTO.getEmail());
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
            logger.debug("为用户分配角色: {}", userDTO.getRoles());
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> {
                            logger.error("角色不存在: {}", roleName);
                            return new RuntimeException("Role not found: " + roleName);
                        });
                roles.add(role);
            }
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        logger.info("用户创建成功，ID: {}, 用户名: {}, 真实姓名: {}", 
                savedUser.getId(), savedUser.getUsername(), savedUser.getRealName());
        return ResponseEntity.ok(convertToDTO(savedUser));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        logger.info("更新用户，ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("用户不存在，ID: {}", id);
                    return new RuntimeException("User not found");
                });
        
        logger.debug("更新前 - 真实姓名: {}, 手机号: {}, 启用状态: {}", 
                user.getRealName(), user.getPhone(), user.getEnabled());
        
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEnabled(userDTO.getEnabled());
        
        if (userDTO.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            logger.debug("更新用户角色: {}", userDTO.getRoles());
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> {
                            logger.error("角色不存在: {}", roleName);
                            return new RuntimeException("Role not found: " + roleName);
                        });
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("用户更新成功，ID: {}, 真实姓名: {}", updatedUser.getId(), updatedUser.getRealName());
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("删除用户，ID: {}", id);
        userRepository.deleteById(id);
        logger.info("用户删除成功，ID: {}", id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("User deleted successfully"));
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        logger.info("重置用户密码，ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("用户不存在，ID: {}", id);
                    return new RuntimeException("User not found");
                });
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        logger.info("用户密码重置成功，ID: {}", id);
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

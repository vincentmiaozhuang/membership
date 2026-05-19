package com.membership.controller;

import com.membership.dto.PermissionDTO;
import com.membership.entity.Permission;
import com.membership.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permissions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PermissionController {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        logger.info("获取所有权限列表");
        List<Permission> permissions = permissionRepository.findAll();
        List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取权限列表，共{}条", permissionDTOs.size());
        return ResponseEntity.ok(permissionDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        logger.info("获取权限详情，ID: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("权限不存在，ID: {}", id);
                    return new RuntimeException("Permission not found");
                });
        logger.info("成功获取权限，ID: {}, 名称: {}", id, permission.getName());
        return ResponseEntity.ok(convertToDTO(permission));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<?> createPermission(@RequestBody PermissionDTO permissionDTO) {
        logger.info("创建权限，名称: {}, 资源: {}, 操作: {}", 
                permissionDTO.getName(), permissionDTO.getResource(), permissionDTO.getAction());
        
        if (permissionRepository.existsByName(permissionDTO.getName())) {
            logger.warn("权限名称已被使用: {}", permissionDTO.getName());
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Permission name is already taken!"));
        }
        
        Permission permission = new Permission();
        permission.setName(permissionDTO.getName());
        permission.setResource(permissionDTO.getResource());
        permission.setAction(permissionDTO.getAction());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission savedPermission = permissionRepository.save(permission);
        logger.info("权限创建成功，ID: {}, 名称: {}", savedPermission.getId(), savedPermission.getName());
        return ResponseEntity.ok(convertToDTO(savedPermission));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @RequestBody PermissionDTO permissionDTO) {
        logger.info("更新权限，ID: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("权限不存在，ID: {}", id);
                    return new RuntimeException("Permission not found");
                });
        
        logger.debug("更新前 - 资源: {}, 操作: {}, 描述: {}", 
                permission.getResource(), permission.getAction(), permission.getDescription());
        
        permission.setResource(permissionDTO.getResource());
        permission.setAction(permissionDTO.getAction());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission updatedPermission = permissionRepository.save(permission);
        logger.info("权限更新成功，ID: {}, 名称: {}", updatedPermission.getId(), updatedPermission.getName());
        return ResponseEntity.ok(convertToDTO(updatedPermission));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        logger.info("删除权限，ID: {}", id);
        permissionRepository.deleteById(id);
        logger.info("权限删除成功，ID: {}", id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Permission deleted successfully"));
    }
    
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}

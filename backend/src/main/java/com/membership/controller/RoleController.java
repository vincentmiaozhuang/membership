package com.membership.controller;

import com.membership.dto.RoleDTO;
import com.membership.entity.Permission;
import com.membership.entity.Role;
import com.membership.repository.PermissionRepository;
import com.membership.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        logger.info("获取所有角色列表");
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取角色列表，共{}条", roleDTOs.size());
        return ResponseEntity.ok(roleDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        logger.info("获取角色详情，ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("角色不存在，ID: {}", id);
                    return new RuntimeException("Role not found");
                });
        logger.info("成功获取角色，ID: {}, 名称: {}", id, role.getName());
        return ResponseEntity.ok(convertToDTO(role));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<?> createRole(@RequestBody RoleDTO roleDTO) {
        logger.info("创建角色，名称: {}, 描述: {}", roleDTO.getName(), roleDTO.getDescription());
        
        if (roleRepository.existsByName(roleDTO.getName())) {
            logger.warn("角色名称已被使用: {}", roleDTO.getName());
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Role name is already taken!"));
        }
        
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        Set<Permission> permissions = new HashSet<>();
        if (roleDTO.getPermissionIds() != null) {
            logger.debug("为角色分配权限，权限ID列表: {}", roleDTO.getPermissionIds());
            for (Long permissionId : roleDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> {
                            logger.error("权限不存在: {}", permissionId);
                            return new RuntimeException("Permission not found: " + permissionId);
                        });
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        
        Role savedRole = roleRepository.save(role);
        logger.info("角色创建成功，ID: {}, 名称: {}, 权限数量: {}", 
                savedRole.getId(), savedRole.getName(), savedRole.getPermissions().size());
        return ResponseEntity.ok(convertToDTO(savedRole));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        logger.info("更新角色，ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("角色不存在，ID: {}", id);
                    return new RuntimeException("Role not found");
                });
        
        logger.debug("更新前 - 名称: {}, 描述: {}", role.getName(), role.getDescription());
        
        role.setDescription(roleDTO.getDescription());
        
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            logger.debug("更新角色权限，权限ID列表: {}", roleDTO.getPermissionIds());
            for (Long permissionId : roleDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> {
                            logger.error("权限不存在: {}", permissionId);
                            return new RuntimeException("Permission not found: " + permissionId);
                        });
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role updatedRole = roleRepository.save(role);
        logger.info("角色更新成功，ID: {}, 名称: {}", updatedRole.getId(), updatedRole.getName());
        return ResponseEntity.ok(convertToDTO(updatedRole));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        logger.info("删除角色，ID: {}", id);
        roleRepository.deleteById(id);
        logger.info("角色删除成功，ID: {}", id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Role deleted successfully"));
    }
    
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setPermissionIds(role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}

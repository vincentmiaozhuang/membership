package com.membership.controller;

import com.membership.dto.RoleDTO;
import com.membership.entity.Permission;
import com.membership.entity.Role;
import com.membership.repository.PermissionRepository;
import com.membership.repository.RoleRepository;
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
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ResponseEntity.ok(convertToDTO(role));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<?> createRole(@RequestBody RoleDTO roleDTO) {
        if (roleRepository.existsByName(roleDTO.getName())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Role name is already taken!"));
        }
        
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        Set<Permission> permissions = new HashSet<>();
        if (roleDTO.getPermissionIds() != null) {
            for (Long permissionId : roleDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.ok(convertToDTO(savedRole));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        role.setDescription(roleDTO.getDescription());
        
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (Long permissionId : roleDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role updatedRole = roleRepository.save(role);
        return ResponseEntity.ok(convertToDTO(updatedRole));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleRepository.deleteById(id);
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

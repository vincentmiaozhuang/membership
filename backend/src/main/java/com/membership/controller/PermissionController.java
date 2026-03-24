package com.membership.controller;

import com.membership.dto.PermissionDTO;
import com.membership.entity.Permission;
import com.membership.repository.PermissionRepository;
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
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissionDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return ResponseEntity.ok(convertToDTO(permission));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<?> createPermission(@RequestBody PermissionDTO permissionDTO) {
        if (permissionRepository.existsByName(permissionDTO.getName())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Permission name is already taken!"));
        }
        
        Permission permission = new Permission();
        permission.setName(permissionDTO.getName());
        permission.setResource(permissionDTO.getResource());
        permission.setAction(permissionDTO.getAction());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission savedPermission = permissionRepository.save(permission);
        return ResponseEntity.ok(convertToDTO(savedPermission));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @RequestBody PermissionDTO permissionDTO) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        permission.setResource(permissionDTO.getResource());
        permission.setAction(permissionDTO.getAction());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission updatedPermission = permissionRepository.save(permission);
        return ResponseEntity.ok(convertToDTO(updatedPermission));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        permissionRepository.deleteById(id);
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

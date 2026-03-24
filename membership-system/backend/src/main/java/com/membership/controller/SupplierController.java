package com.membership.controller;

import com.membership.dto.SupplierDTO;
import com.membership.entity.Supplier;
import com.membership.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/suppliers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupplierController {
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        return ResponseEntity.ok(convertToDTO(supplier));
    }
    
    @GetMapping("/enabled")
    public ResponseEntity<List<SupplierDTO>> getEnabledSuppliers() {
        List<Supplier> suppliers = supplierRepository.findByEnabledTrue();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<List<SupplierDTO>> searchSuppliers(@RequestParam String name) {
        List<Supplier> suppliers = supplierRepository.findByNameContaining(name);
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier:create')")
    public ResponseEntity<?> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setSupplierCode(generateSupplierCode());
        supplier.setCooperationStartDate(supplierDTO.getCooperationStartDate());
        supplier.setEnabled(supplierDTO.getEnabled() != null ? supplierDTO.getEnabled() : true);
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(convertToDTO(savedSupplier));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:update')")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        supplier.setName(supplierDTO.getName());
        supplier.setCooperationStartDate(supplierDTO.getCooperationStartDate());
        supplier.setEnabled(supplierDTO.getEnabled());
        
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(convertToDTO(updatedSupplier));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:delete')")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        supplierRepository.deleteById(id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Supplier deleted successfully"));
    }
    
    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setSupplierCode(supplier.getSupplierCode());
        dto.setCooperationStartDate(supplier.getCooperationStartDate());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        dto.setEnabled(supplier.getEnabled());
        return dto;
    }
    
    private String generateSupplierCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        // 检查是否已存在，避免重复
        while (supplierRepository.existsBySupplierCode(code.toString())) {
            code.setLength(0);
            for (int i = 0; i < 16; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        return code.toString();
    }
}

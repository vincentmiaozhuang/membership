package com.membership.controller;

import com.membership.dto.SupplierDTO;
import com.membership.core.entity.Supplier;
import com.membership.core.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        logger.info("获取所有供应商列表");
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取供应商列表，共{}条", supplierDTOs.size());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        logger.info("获取供应商详情，ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("供应商不存在，ID: {}", id);
                    return new RuntimeException("Supplier not found");
                });
        logger.info("成功获取供应商，ID: {}, 名称: {}", id, supplier.getName());
        return ResponseEntity.ok(convertToDTO(supplier));
    }
    
    @GetMapping("/enabled")
    public ResponseEntity<List<SupplierDTO>> getEnabledSuppliers() {
        logger.info("获取可用供应商列表");
        List<Supplier> suppliers = supplierRepository.findByEnabledTrue();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取可用供应商列表，共{}条", supplierDTOs.size());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('supplier:read')")
    public ResponseEntity<List<SupplierDTO>> searchSuppliers(@RequestParam String name) {
        logger.info("搜索供应商，关键词: {}", name);
        List<Supplier> suppliers = supplierRepository.findByNameContaining(name);
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("搜索完成，找到{}条匹配的供应商", supplierDTOs.size());
        return ResponseEntity.ok(supplierDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier:create')")
    public ResponseEntity<?> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        logger.info("创建供应商，名称: {}", supplierDTO.getName());
        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setSupplierCode(generateSupplierCode());
        supplier.setCooperationStartDate(supplierDTO.getCooperationStartDate());
        supplier.setEnabled(supplierDTO.getEnabled() != null ? supplierDTO.getEnabled() : true);
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        logger.info("供应商创建成功，ID: {}, 名称: {}, 供应商代码: {}", 
                savedSupplier.getId(), savedSupplier.getName(), savedSupplier.getSupplierCode());
        return ResponseEntity.ok(convertToDTO(savedSupplier));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:update')")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        logger.info("更新供应商，ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("供应商不存在，ID: {}", id);
                    return new RuntimeException("Supplier not found");
                });
        
        supplier.setName(supplierDTO.getName());
        supplier.setCooperationStartDate(supplierDTO.getCooperationStartDate());
        supplier.setEnabled(supplierDTO.getEnabled());
        
        Supplier updatedSupplier = supplierRepository.save(supplier);
        logger.info("供应商更新成功，ID: {}, 名称: {}", updatedSupplier.getId(), updatedSupplier.getName());
        return ResponseEntity.ok(convertToDTO(updatedSupplier));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:delete')")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        logger.info("删除供应商，ID: {}", id);
        supplierRepository.deleteById(id);
        logger.info("供应商删除成功，ID: {}", id);
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

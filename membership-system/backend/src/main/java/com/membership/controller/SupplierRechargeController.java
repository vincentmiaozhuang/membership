package com.membership.controller;

import com.membership.dto.SupplierRechargeDTO;
import com.membership.entity.Supplier;
import com.membership.entity.SupplierRecharge;
import com.membership.repository.SupplierRechargeRepository;
import com.membership.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-recharges")
public class SupplierRechargeController {
    
    @Autowired
    private SupplierRechargeRepository supplierRechargeRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-recharge:read')")
    public ResponseEntity<?> getSupplierRecharges() {
        List<SupplierRecharge> supplierRecharges = supplierRechargeRepository.findAll();
        List<SupplierRechargeDTO> supplierRechargeDTOs = supplierRecharges.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierRechargeDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-recharge:create')")
    public ResponseEntity<?> createSupplierRecharge(@RequestBody SupplierRechargeDTO supplierRechargeDTO) {
        SupplierRecharge supplierRecharge = new SupplierRecharge();
        supplierRecharge.setSupplierId(supplierRechargeDTO.getSupplierId());
        supplierRecharge.setAmount(supplierRechargeDTO.getAmount());
        supplierRecharge.setScreenshotUrl(supplierRechargeDTO.getScreenshotUrl());
        supplierRecharge.setOperatorId(supplierRechargeDTO.getOperatorId());
        supplierRecharge.setOperatorName(supplierRechargeDTO.getOperatorName());
        supplierRecharge.setEnabled(supplierRechargeDTO.getEnabled() != null ? supplierRechargeDTO.getEnabled() : true);
        
        SupplierRecharge savedSupplierRecharge = supplierRechargeRepository.save(supplierRecharge);
        return ResponseEntity.ok(convertToDTO(savedSupplierRecharge));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-recharge:update')")
    public ResponseEntity<?> updateSupplierRecharge(@PathVariable Long id, @RequestBody SupplierRechargeDTO supplierRechargeDTO) {
        SupplierRecharge supplierRecharge = supplierRechargeRepository.findById(id).orElse(null);
        if (supplierRecharge == null) {
            return ResponseEntity.notFound().build();
        }
        
        supplierRecharge.setSupplierId(supplierRechargeDTO.getSupplierId());
        supplierRecharge.setAmount(supplierRechargeDTO.getAmount());
        supplierRecharge.setScreenshotUrl(supplierRechargeDTO.getScreenshotUrl());
        supplierRecharge.setOperatorId(supplierRechargeDTO.getOperatorId());
        supplierRecharge.setOperatorName(supplierRechargeDTO.getOperatorName());
        supplierRecharge.setEnabled(supplierRechargeDTO.getEnabled() != null ? supplierRechargeDTO.getEnabled() : true);
        
        SupplierRecharge updatedSupplierRecharge = supplierRechargeRepository.save(supplierRecharge);
        return ResponseEntity.ok(convertToDTO(updatedSupplierRecharge));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-recharge:delete')")
    public ResponseEntity<?> deleteSupplierRecharge(@PathVariable Long id) {
        supplierRechargeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    private SupplierRechargeDTO convertToDTO(SupplierRecharge supplierRecharge) {
        SupplierRechargeDTO dto = new SupplierRechargeDTO();
        dto.setId(supplierRecharge.getId());
        dto.setSupplierId(supplierRecharge.getSupplierId());
        dto.setAmount(supplierRecharge.getAmount());
        dto.setScreenshotUrl(supplierRecharge.getScreenshotUrl());
        dto.setOperatorId(supplierRecharge.getOperatorId());
        dto.setOperatorName(supplierRecharge.getOperatorName());
        dto.setEnabled(supplierRecharge.getEnabled());
        dto.setCreatedAt(supplierRecharge.getCreatedAt());
        dto.setUpdatedAt(supplierRecharge.getUpdatedAt());
        
        // 关联供应商信息
        Supplier supplier = supplierRepository.findById(supplierRecharge.getSupplierId()).orElse(null);
        if (supplier != null) {
            dto.setSupplierName(supplier.getName());
            dto.setSupplierCode(supplier.getSupplierCode());
        }
        
        return dto;
    }
}
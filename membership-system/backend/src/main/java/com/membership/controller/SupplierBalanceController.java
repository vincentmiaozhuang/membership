package com.membership.controller;

import com.membership.dto.SupplierBalanceDTO;
import com.membership.entity.Supplier;
import com.membership.entity.SupplierBalance;
import com.membership.repository.SupplierBalanceRepository;
import com.membership.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-balances")
public class SupplierBalanceController {
    
    @Autowired
    private SupplierBalanceRepository supplierBalanceRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-balance:read')")
    public ResponseEntity<?> getSupplierBalances() {
        List<SupplierBalance> supplierBalances = supplierBalanceRepository.findAll();
        List<SupplierBalanceDTO> supplierBalanceDTOs = supplierBalances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierBalanceDTOs);
    }
    
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('supplier-balance:read')")
    public ResponseEntity<?> getSupplierBalanceBySupplierId(@PathVariable Long supplierId) {
        SupplierBalance supplierBalance = supplierBalanceRepository.findBySupplierId(supplierId).orElse(null);
        if (supplierBalance == null) {
            // 如果不存在，创建一个新的余额记录
            supplierBalance = new SupplierBalance();
            supplierBalance.setSupplierId(supplierId);
            supplierBalance = supplierBalanceRepository.save(supplierBalance);
        }
        return ResponseEntity.ok(convertToDTO(supplierBalance));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-balance:create')")
    public ResponseEntity<?> createSupplierBalance(@RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        // 检查是否已存在该供应商的余额记录
        SupplierBalance existingBalance = supplierBalanceRepository.findBySupplierId(supplierBalanceDTO.getSupplierId()).orElse(null);
        if (existingBalance != null) {
            return ResponseEntity.badRequest().body("该供应商的余额记录已存在");
        }
        
        SupplierBalance supplierBalance = new SupplierBalance();
        supplierBalance.setSupplierId(supplierBalanceDTO.getSupplierId());
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance savedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        return ResponseEntity.ok(convertToDTO(savedSupplierBalance));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-balance:update')")
    public ResponseEntity<?> updateSupplierBalance(@PathVariable Long id, @RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        SupplierBalance supplierBalance = supplierBalanceRepository.findById(id).orElse(null);
        if (supplierBalance == null) {
            return ResponseEntity.notFound().build();
        }
        
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance updatedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        return ResponseEntity.ok(convertToDTO(updatedSupplierBalance));
    }
    
    @PutMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('supplier-balance:update')")
    public ResponseEntity<?> updateSupplierBalanceBySupplierId(@PathVariable Long supplierId, @RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        SupplierBalance supplierBalance = supplierBalanceRepository.findBySupplierId(supplierId).orElse(null);
        if (supplierBalance == null) {
            // 如果不存在，创建一个新的余额记录
            supplierBalance = new SupplierBalance();
            supplierBalance.setSupplierId(supplierId);
        }
        
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance updatedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        return ResponseEntity.ok(convertToDTO(updatedSupplierBalance));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-balance:delete')")
    public ResponseEntity<?> deleteSupplierBalance(@PathVariable Long id) {
        supplierBalanceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    private SupplierBalanceDTO convertToDTO(SupplierBalance supplierBalance) {
        SupplierBalanceDTO dto = new SupplierBalanceDTO();
        dto.setId(supplierBalance.getId());
        dto.setSupplierId(supplierBalance.getSupplierId());
        dto.setTotalRecharge(supplierBalance.getTotalRecharge());
        dto.setConsumedAmount(supplierBalance.getConsumedAmount());
        dto.setRemainingAmount(supplierBalance.getRemainingAmount());
        dto.setAlertThreshold(supplierBalance.getAlertThreshold());
        dto.setCreatedAt(supplierBalance.getCreatedAt());
        dto.setUpdatedAt(supplierBalance.getUpdatedAt());
        
        // 关联供应商信息
        Supplier supplier = supplierRepository.findById(supplierBalance.getSupplierId()).orElse(null);
        if (supplier != null) {
            dto.setSupplierName(supplier.getName());
            dto.setSupplierCode(supplier.getSupplierCode());
        }
        
        return dto;
    }
}
package com.membership.controller;

import com.membership.dto.SupplierBalanceDTO;
import com.membership.core.entity.Supplier;
import com.membership.core.entity.SupplierBalance;
import com.membership.core.repository.SupplierBalanceRepository;
import com.membership.core.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-balances")
public class SupplierBalanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierBalanceController.class);
    
    @Autowired
    private SupplierBalanceRepository supplierBalanceRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-balance:read')")
    public ResponseEntity<?> getSupplierBalances() {
        logger.info("获取所有供应商余额列表");
        List<SupplierBalance> supplierBalances = supplierBalanceRepository.findAll();
        List<SupplierBalanceDTO> supplierBalanceDTOs = supplierBalances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取供应商余额列表，共{}条", supplierBalanceDTOs.size());
        return ResponseEntity.ok(supplierBalanceDTOs);
    }
    
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('supplier-balance:read')")
    public ResponseEntity<?> getSupplierBalanceBySupplierId(@PathVariable Long supplierId) {
        logger.info("获取供应商余额，供应商ID: {}", supplierId);
        SupplierBalance supplierBalance = supplierBalanceRepository.findBySupplierId(supplierId).orElse(null);
        if (supplierBalance == null) {
            logger.info("供应商[{}]不存在余额记录，创建新记录", supplierId);
            // 如果不存在，创建一个新的余额记录
            supplierBalance = new SupplierBalance();
            supplierBalance.setSupplierId(supplierId);
            supplierBalance = supplierBalanceRepository.save(supplierBalance);
        }
        logger.info("成功获取供应商余额，ID: {}, 供应商ID: {}", supplierBalance.getId(), supplierId);
        return ResponseEntity.ok(convertToDTO(supplierBalance));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-balance:create')")
    public ResponseEntity<?> createSupplierBalance(@RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        logger.info("创建供应商余额记录，供应商ID: {}", supplierBalanceDTO.getSupplierId());
        
        // 检查是否已存在该供应商的余额记录
        SupplierBalance existingBalance = supplierBalanceRepository.findBySupplierId(supplierBalanceDTO.getSupplierId()).orElse(null);
        if (existingBalance != null) {
            logger.warn("供应商[{}]的余额记录已存在", supplierBalanceDTO.getSupplierId());
            return ResponseEntity.badRequest().body("该供应商的余额记录已存在");
        }
        
        SupplierBalance supplierBalance = new SupplierBalance();
        supplierBalance.setSupplierId(supplierBalanceDTO.getSupplierId());
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance savedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        logger.info("供应商余额记录创建成功，ID: {}, 供应商ID: {}, 充值总额: {}", 
                savedSupplierBalance.getId(), savedSupplierBalance.getSupplierId(), savedSupplierBalance.getTotalRecharge());
        return ResponseEntity.ok(convertToDTO(savedSupplierBalance));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-balance:update')")
    public ResponseEntity<?> updateSupplierBalance(@PathVariable Long id, @RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        logger.info("更新供应商余额记录，ID: {}", id);
        SupplierBalance supplierBalance = supplierBalanceRepository.findById(id).orElse(null);
        if (supplierBalance == null) {
            logger.warn("供应商余额记录不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        logger.debug("更新前 - 充值总额: {}, 已消费金额: {}, 剩余金额: {}", 
                supplierBalance.getTotalRecharge(), supplierBalance.getConsumedAmount(), supplierBalance.getRemainingAmount());
        
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance updatedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        logger.info("供应商余额记录更新成功，ID: {}, 供应商ID: {}", updatedSupplierBalance.getId(), updatedSupplierBalance.getSupplierId());
        return ResponseEntity.ok(convertToDTO(updatedSupplierBalance));
    }
    
    @PutMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('supplier-balance:update')")
    public ResponseEntity<?> updateSupplierBalanceBySupplierId(@PathVariable Long supplierId, @RequestBody SupplierBalanceDTO supplierBalanceDTO) {
        logger.info("按供应商ID更新余额记录，供应商ID: {}", supplierId);
        SupplierBalance supplierBalance = supplierBalanceRepository.findBySupplierId(supplierId).orElse(null);
        if (supplierBalance == null) {
            logger.info("供应商[{}]不存在余额记录，创建新记录", supplierId);
            // 如果不存在，创建一个新的余额记录
            supplierBalance = new SupplierBalance();
            supplierBalance.setSupplierId(supplierId);
        }
        
        supplierBalance.setTotalRecharge(supplierBalanceDTO.getTotalRecharge());
        supplierBalance.setConsumedAmount(supplierBalanceDTO.getConsumedAmount());
        supplierBalance.setRemainingAmount(supplierBalanceDTO.getRemainingAmount());
        supplierBalance.setAlertThreshold(supplierBalanceDTO.getAlertThreshold());
        
        SupplierBalance updatedSupplierBalance = supplierBalanceRepository.save(supplierBalance);
        logger.info("供应商余额记录更新成功，ID: {}, 供应商ID: {}", updatedSupplierBalance.getId(), supplierId);
        return ResponseEntity.ok(convertToDTO(updatedSupplierBalance));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-balance:delete')")
    public ResponseEntity<?> deleteSupplierBalance(@PathVariable Long id) {
        logger.info("删除供应商余额记录，ID: {}", id);
        supplierBalanceRepository.deleteById(id);
        logger.info("供应商余额记录删除成功，ID: {}", id);
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
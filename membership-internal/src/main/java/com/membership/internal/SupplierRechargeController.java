package com.membership.controller;

import com.membership.dto.SupplierRechargeDTO;
import com.membership.core.entity.Supplier;
import com.membership.core.entity.SupplierBalance;
import com.membership.core.entity.SupplierRecharge;
import com.membership.core.repository.SupplierBalanceRepository;
import com.membership.core.repository.SupplierRechargeRepository;
import com.membership.core.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-recharges")
public class SupplierRechargeController {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierRechargeController.class);
    
    @Autowired
    private SupplierRechargeRepository supplierRechargeRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private SupplierBalanceRepository supplierBalanceRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-recharge:read')")
    public ResponseEntity<?> getSupplierRecharges() {
        logger.info("获取所有供应商充值记录");
        List<SupplierRecharge> supplierRecharges = supplierRechargeRepository.findAll();
        List<SupplierRechargeDTO> supplierRechargeDTOs = supplierRecharges.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取供应商充值记录，共{}条", supplierRechargeDTOs.size());
        return ResponseEntity.ok(supplierRechargeDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-recharge:create')")
    public ResponseEntity<?> createSupplierRecharge(@RequestBody SupplierRechargeDTO supplierRechargeDTO) {
        logger.info("创建供应商充值记录，供应商ID: {}, 充值金额: {}", 
                supplierRechargeDTO.getSupplierId(), supplierRechargeDTO.getAmount());
        logger.debug("操作人ID: {}, 操作人名称: {}", 
                supplierRechargeDTO.getOperatorId(), supplierRechargeDTO.getOperatorName());
        
        SupplierRecharge supplierRecharge = new SupplierRecharge();
        supplierRecharge.setSupplierId(supplierRechargeDTO.getSupplierId());
        supplierRecharge.setAmount(supplierRechargeDTO.getAmount());
        supplierRecharge.setScreenshotUrl(supplierRechargeDTO.getScreenshotUrl());
        supplierRecharge.setOperatorId(supplierRechargeDTO.getOperatorId());
        supplierRecharge.setOperatorName(supplierRechargeDTO.getOperatorName());
        supplierRecharge.setEnabled(supplierRechargeDTO.getEnabled() != null ? supplierRechargeDTO.getEnabled() : true);
        
        SupplierRecharge savedSupplierRecharge = supplierRechargeRepository.save(supplierRecharge);
        logger.info("供应商充值记录创建成功，ID: {}, 供应商ID: {}, 充值金额: {}", 
                savedSupplierRecharge.getId(), savedSupplierRecharge.getSupplierId(), savedSupplierRecharge.getAmount());
        
        // 更新供应商余额
        updateSupplierBalance(supplierRechargeDTO.getSupplierId());
        
        return ResponseEntity.ok(convertToDTO(savedSupplierRecharge));
    }
    
    private void updateSupplierBalance(Long supplierId) {
        logger.debug("更新供应商余额，供应商ID: {}", supplierId);
        
        // 查找或创建供应商余额记录
        Optional<SupplierBalance> existingBalance = supplierBalanceRepository.findBySupplierId(supplierId);
        SupplierBalance supplierBalance = existingBalance.orElse(new SupplierBalance());
        supplierBalance.setSupplierId(supplierId);
        
        // 计算该供应商的账户充值总额
        BigDecimal totalRecharge = supplierRechargeRepository.findBySupplierId(supplierId)
                .stream()
                .map(SupplierRecharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.debug("供应商[{}]充值总额计算完成: {}", supplierId, totalRecharge);
        
        // 获取当前已消耗金额
        BigDecimal consumedAmount = supplierBalance.getConsumedAmount() != null ? supplierBalance.getConsumedAmount() : BigDecimal.ZERO;
        logger.debug("供应商[{}]已消耗金额: {}", supplierId, consumedAmount);
        
        // 计算剩余金额 = 充值总额 - 已消耗金额
        BigDecimal remainingAmount = totalRecharge.subtract(consumedAmount);
        logger.debug("供应商[{}]剩余金额计算完成: {}", supplierId, remainingAmount);
        
        // 更新供应商余额
        supplierBalance.setTotalRecharge(totalRecharge);
        supplierBalance.setRemainingAmount(remainingAmount);
        supplierBalanceRepository.save(supplierBalance);
        
        logger.info("供应商[{}]余额更新完成，充值总额: {}, 剩余金额: {}", supplierId, totalRecharge, remainingAmount);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-recharge:update')")
    public ResponseEntity<?> updateSupplierRecharge(@PathVariable Long id, @RequestBody SupplierRechargeDTO supplierRechargeDTO) {
        logger.info("更新供应商充值记录，ID: {}", id);
        SupplierRecharge supplierRecharge = supplierRechargeRepository.findById(id).orElse(null);
        if (supplierRecharge == null) {
            logger.warn("供应商充值记录不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        logger.debug("更新前 - 供应商ID: {}, 充值金额: {}", 
                supplierRecharge.getSupplierId(), supplierRecharge.getAmount());
        
        supplierRecharge.setSupplierId(supplierRechargeDTO.getSupplierId());
        supplierRecharge.setAmount(supplierRechargeDTO.getAmount());
        supplierRecharge.setScreenshotUrl(supplierRechargeDTO.getScreenshotUrl());
        supplierRecharge.setOperatorId(supplierRechargeDTO.getOperatorId());
        supplierRecharge.setOperatorName(supplierRechargeDTO.getOperatorName());
        supplierRecharge.setEnabled(supplierRechargeDTO.getEnabled() != null ? supplierRechargeDTO.getEnabled() : true);
        
        SupplierRecharge updatedSupplierRecharge = supplierRechargeRepository.save(supplierRecharge);
        logger.info("供应商充值记录更新成功，ID: {}, 供应商ID: {}, 充值金额: {}", 
                updatedSupplierRecharge.getId(), updatedSupplierRecharge.getSupplierId(), updatedSupplierRecharge.getAmount());
        
        return ResponseEntity.ok(convertToDTO(updatedSupplierRecharge));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-recharge:delete')")
    public ResponseEntity<?> deleteSupplierRecharge(@PathVariable Long id) {
        logger.info("删除供应商充值记录，ID: {}", id);
        supplierRechargeRepository.deleteById(id);
        logger.info("供应商充值记录删除成功，ID: {}", id);
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
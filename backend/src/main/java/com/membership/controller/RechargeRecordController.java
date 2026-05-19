package com.membership.controller;

import com.membership.dto.RechargeRecordDTO;
import com.membership.entity.RechargeRecord;
import com.membership.repository.RechargeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recharge-records")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RechargeRecordController {
    
    private static final Logger logger = LoggerFactory.getLogger(RechargeRecordController.class);
    
    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getAllRechargeRecords() {
        logger.info("获取所有充值记录列表");
        List<RechargeRecord> records = rechargeRecordRepository.findAll();
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取充值记录列表，共{}条", recordDTOs.size());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<RechargeRecordDTO> getRechargeRecordById(@PathVariable Long id) {
        logger.info("获取充值记录详情，ID: {}", id);
        RechargeRecord record = rechargeRecordRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("充值记录不存在，ID: {}", id);
                    return new RuntimeException("Recharge record not found");
                });
        logger.info("成功获取充值记录，ID: {}, 产品名称: {}", id, record.getProductName());
        return ResponseEntity.ok(convertToDTO(record));
    }
    
    @GetMapping("/phone/{rechargePhone}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByPhone(@PathVariable String rechargePhone) {
        logger.info("按手机号查询充值记录，手机号: {}", rechargePhone);
        // 这里需要修改，因为我们已经将 phone 字段改为 rechargePhone
        // 暂时返回空列表，后续需要根据实际情况修改
        logger.debug("当前方法暂未实现，返回空列表");
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByStatus(@PathVariable String status) {
        logger.info("按状态查询充值记录，状态: {}", status);
        List<RechargeRecord> records = rechargeRecordRepository.findByStatus(status);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取状态[{}]的充值记录，共{}条", status, recordDTOs.size());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/product/{productName}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByProductName(@PathVariable String productName) {
        logger.info("按产品名称查询充值记录，产品名称: {}", productName);
        List<RechargeRecord> records = rechargeRecordRepository.findByProductName(productName);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取产品[{}]的充值记录，共{}条", productName, recordDTOs.size());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/supplier/{supplierName}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsBySupplierName(@PathVariable String supplierName) {
        logger.info("按供应商名称查询充值记录，供应商名称: {}", supplierName);
        List<RechargeRecord> records = rechargeRecordRepository.findBySupplierName(supplierName);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取供应商[{}]的充值记录，共{}条", supplierName, recordDTOs.size());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('recharge:create')")
    public ResponseEntity<?> createRechargeRecord(@RequestBody RechargeRecordDTO rechargeRecordDTO) {
        logger.info("创建充值记录，产品名称: {}, 客户名称: {}, 状态: {}", 
                rechargeRecordDTO.getProductName(), rechargeRecordDTO.getCustomerName(), rechargeRecordDTO.getStatus());
        logger.debug("客户价格: {}, 供应商价格: {}", 
                rechargeRecordDTO.getCustomerPrice(), rechargeRecordDTO.getSupplierPrice());
        
        RechargeRecord record = new RechargeRecord();
        record.setCustomerProductId(rechargeRecordDTO.getCustomerProductId());
        record.setProductId(rechargeRecordDTO.getProductId());
        record.setProductName(rechargeRecordDTO.getProductName());
        record.setRechargePhone(rechargeRecordDTO.getRechargePhone());
        record.setCustomerOrderId(rechargeRecordDTO.getCustomerOrderId());
        record.setPlatformOrderId(rechargeRecordDTO.getPlatformOrderId());
        record.setCustomerId(rechargeRecordDTO.getCustomerId());
        record.setCustomerName(rechargeRecordDTO.getCustomerName());
        record.setCustomerPrice(rechargeRecordDTO.getCustomerPrice());
        record.setSupplierId(rechargeRecordDTO.getSupplierId());
        record.setSupplierName(rechargeRecordDTO.getSupplierName());
        record.setSupplierPrice(rechargeRecordDTO.getSupplierPrice());
        record.setProductFacePrice(rechargeRecordDTO.getProductFacePrice());
        record.setStatus(rechargeRecordDTO.getStatus());
        record.setDescription(rechargeRecordDTO.getDescription());
        record.setRechargePerson(rechargeRecordDTO.getRechargePerson());
        
        RechargeRecord savedRecord = rechargeRecordRepository.save(record);
        logger.info("充值记录创建成功，ID: {}, 产品名称: {}, 客户名称: {}", 
                savedRecord.getId(), savedRecord.getProductName(), savedRecord.getCustomerName());
        return ResponseEntity.ok(convertToDTO(savedRecord));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('recharge:delete')")
    public ResponseEntity<?> deleteRechargeRecord(@PathVariable Long id) {
        logger.info("删除充值记录，ID: {}", id);
        rechargeRecordRepository.deleteById(id);
        logger.info("充值记录删除成功，ID: {}", id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Recharge record deleted successfully"));
    }
    
    private RechargeRecordDTO convertToDTO(RechargeRecord record) {
        RechargeRecordDTO dto = new RechargeRecordDTO();
        dto.setId(record.getId());
        dto.setCustomerProductId(record.getCustomerProductId());
        dto.setProductId(record.getProductId());
        dto.setProductName(record.getProductName());
        dto.setRechargePhone(record.getRechargePhone());
        dto.setCustomerOrderId(record.getCustomerOrderId());
        dto.setPlatformOrderId(record.getPlatformOrderId());
        dto.setCustomerId(record.getCustomerId());
        dto.setCustomerName(record.getCustomerName());
        dto.setCustomerPrice(record.getCustomerPrice());
        dto.setSupplierId(record.getSupplierId());
        dto.setSupplierName(record.getSupplierName());
        dto.setSupplierPrice(record.getSupplierPrice());
        dto.setProductFacePrice(record.getProductFacePrice());
        dto.setStatus(record.getStatus());
        dto.setDescription(record.getDescription());
        dto.setRechargePerson(record.getRechargePerson());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}

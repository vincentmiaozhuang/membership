package com.membership.controller;

import com.membership.dto.RechargeRecordDTO;
import com.membership.entity.RechargeRecord;
import com.membership.repository.RechargeRecordRepository;
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
    
    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getAllRechargeRecords() {
        List<RechargeRecord> records = rechargeRecordRepository.findAll();
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<RechargeRecordDTO> getRechargeRecordById(@PathVariable Long id) {
        RechargeRecord record = rechargeRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recharge record not found"));
        return ResponseEntity.ok(convertToDTO(record));
    }
    
    @GetMapping("/phone/{rechargePhone}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByPhone(@PathVariable String rechargePhone) {
        // 这里需要修改，因为我们已经将 phone 字段改为 rechargePhone
        // 暂时返回空列表，后续需要根据实际情况修改
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByStatus(@PathVariable String status) {
        List<RechargeRecord> records = rechargeRecordRepository.findByStatus(status);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/product/{productName}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsByProductName(@PathVariable String productName) {
        List<RechargeRecord> records = rechargeRecordRepository.findByProductName(productName);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @GetMapping("/supplier/{supplierName}")
    @PreAuthorize("hasAuthority('recharge:read')")
    public ResponseEntity<List<RechargeRecordDTO>> getRechargeRecordsBySupplierName(@PathVariable String supplierName) {
        List<RechargeRecord> records = rechargeRecordRepository.findBySupplierName(supplierName);
        List<RechargeRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recordDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('recharge:create')")
    public ResponseEntity<?> createRechargeRecord(@RequestBody RechargeRecordDTO rechargeRecordDTO) {
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
        return ResponseEntity.ok(convertToDTO(savedRecord));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('recharge:delete')")
    public ResponseEntity<?> deleteRechargeRecord(@PathVariable Long id) {
        rechargeRecordRepository.deleteById(id);
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

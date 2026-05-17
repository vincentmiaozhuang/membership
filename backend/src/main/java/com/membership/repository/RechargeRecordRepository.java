package com.membership.repository;

import com.membership.entity.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    
    List<RechargeRecord> findByRechargePhone(String rechargePhone);
    
    List<RechargeRecord> findByStatus(String status);
    
    List<RechargeRecord> findByProductName(String productName);
    
    List<RechargeRecord> findBySupplierName(String supplierName);
    
    List<RechargeRecord> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}

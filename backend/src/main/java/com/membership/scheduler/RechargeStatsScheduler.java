package com.membership.scheduler;

import com.membership.entity.RechargeRecord;
import com.membership.entity.RechargeStats;
import com.membership.repository.RechargeRecordRepository;
import com.membership.repository.RechargeStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RechargeStatsScheduler {

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;

    @Autowired
    private RechargeStatsRepository rechargeStatsRepository;

    /**
     * 每天凌晨2点执行，计算前一天的统计数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void calculateRechargeStats() {
        // 计算前一天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);
        calculateRechargeStatsByDate(yesterday);
    }

    /**
     * 按指定日期计算统计数据
     * @param date 指定的日期
     */
    @Transactional
    public void calculateRechargeStatsByDate(LocalDate date) {
        log.info("开始计算指定日期的充值统计数据: {}", date);
        
        try {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
            
            log.info("统计日期: {}", date);
            log.info("开始时间: {}", startDateTime);
            log.info("结束时间: {}", endDateTime);
            
            // 查询指定日期的所有充值记录
            List<RechargeRecord> records = rechargeRecordRepository.findByCreatedAtBetween(startDateTime, endDateTime);
            log.info("查询到 {} 条充值记录", records.size());
            
            if (records.isEmpty()) {
                log.info("没有找到充值记录，统计结束");
                return;
            }
            
            // 按维度分组
            Map<String, List<RechargeRecord>> groupedRecords = records.stream()
                .collect(Collectors.groupingBy(record -> 
                    record.getProductId() + "_" + record.getCustomerId() + "_" + record.getSupplierId() + "_" + record.getStatus()
                ));
            
            log.info("分组后得到 {} 组数据", groupedRecords.size());
            
            // 处理每组数据
            int processedGroups = 0;
            for (Map.Entry<String, List<RechargeRecord>> entry : groupedRecords.entrySet()) {
                List<RechargeRecord> groupRecords = entry.getValue();
                if (groupRecords.isEmpty()) {
                    continue;
                }
                
                // 获取分组维度信息
                RechargeRecord firstRecord = groupRecords.get(0);
                Long productId = firstRecord.getProductId();
                String productName = firstRecord.getProductName();
                BigDecimal productFacePrice = firstRecord.getProductFacePrice();
                Long customerId = firstRecord.getCustomerId();
                String customerName = firstRecord.getCustomerName();
                Long supplierId = firstRecord.getSupplierId();
                String supplierName = firstRecord.getSupplierName();
                String status = firstRecord.getStatus();
                BigDecimal customerPrice = firstRecord.getCustomerPrice();
                BigDecimal supplierPrice = firstRecord.getSupplierPrice();
                
                // 空值检查
                if (productId == null || customerId == null || supplierId == null || 
                    productName == null || customerName == null || supplierName == null || 
                    status == null || customerPrice == null || supplierPrice == null) {
                    log.warn("跳过无效记录: 缺少必要字段");
                    log.warn("产品ID: {}, 客户ID: {}, 供应商ID: {}", productId, customerId, supplierId);
                    log.warn("产品名称: {}, 客户名称: {}, 供应商名称: {}", productName, customerName, supplierName);
                    log.warn("状态: {}, 客户价格: {}, 供应商价格: {}", status, customerPrice, supplierPrice);
                    continue;
                }
                
                // 计算统计数据
                int rechargeCount = groupRecords.size();
                BigDecimal customerAmount = customerPrice.multiply(new BigDecimal(rechargeCount));
                BigDecimal costAmount = supplierPrice.multiply(new BigDecimal(rechargeCount));
                
                log.info("处理分组: 产品={}, 客户={}, 供应商={}, 状态={}, 数量={}", 
                    productName, customerName, supplierName, status, rechargeCount);
                
                try {
                    // 检查是否已存在记录
                    Date statDate = new Date(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    RechargeStats existingStats = rechargeStatsRepository.findByStatDateAndProductIdAndCustomerIdAndSupplierIdAndStatus(
                        statDate,
                        productId,
                        customerId,
                        supplierId,
                        status
                    );
                    
                    if (existingStats != null) {
                        // 更新现有记录
                        existingStats.setRechargeCount(rechargeCount);
                        existingStats.setCustomerAmount(customerAmount);
                        existingStats.setCostAmount(costAmount);
                        rechargeStatsRepository.save(existingStats);
                        log.info("更新统计记录成功");
                    } else {
                        // 创建新记录
                        RechargeStats newStats = new RechargeStats();
                        newStats.setStatDate(statDate);
                        newStats.setProductId(productId);
                        newStats.setProductName(productName);
                        newStats.setProductFacePrice(productFacePrice);
                        newStats.setCustomerId(customerId);
                        newStats.setCustomerName(customerName);
                        newStats.setSupplierId(supplierId);
                        newStats.setSupplierName(supplierName);
                        newStats.setStatus(status);
                        newStats.setRechargeCount(rechargeCount);
                        newStats.setCustomerPrice(customerPrice);
                        newStats.setCustomerAmount(customerAmount);
                        newStats.setSupplierPrice(supplierPrice);
                        newStats.setCostAmount(costAmount);
                        rechargeStatsRepository.save(newStats);
                        log.info("创建统计记录成功");
                    }
                    processedGroups++;
                } catch (Exception e) {
                    log.error("处理分组数据失败", e);
                }
            }
            
            log.info("充值统计数据计算完成，共处理 {} 条记录，{} 个分组", records.size(), processedGroups);
        } catch (Exception e) {
            log.error("计算充值统计数据失败", e);
        }
    }
}
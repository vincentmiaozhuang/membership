package com.membership.controller;

import com.membership.dto.RechargeStatsDTO;
import com.membership.entity.RechargeStats;
import com.membership.repository.RechargeStatsRepository;
import com.membership.scheduler.RechargeStatsScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private RechargeStatsScheduler rechargeStatsScheduler;

    @Autowired
    private RechargeStatsRepository rechargeStatsRepository;

    /**
     * 手动触发充值统计计算
     * @param date 指定的日期，格式：yyyy-MM-dd，若不指定则计算前一天的
     */
    @GetMapping("/calculate")
    public String calculateStats(@RequestParam(required = false) String date) {
        if (date != null && !date.isEmpty()) {
            try {
                // 解析指定的日期
                java.time.LocalDate targetDate = java.time.LocalDate.parse(date);
                rechargeStatsScheduler.calculateRechargeStatsByDate(targetDate);
                return "统计数据计算任务已触发，计算日期: " + date;
            } catch (Exception e) {
                e.printStackTrace();
                return "日期格式错误，请使用 yyyy-MM-dd 格式";
            }
        } else {
            // 不指定日期则计算前一天的
            rechargeStatsScheduler.calculateRechargeStats();
            return "统计数据计算任务已触发，计算前一天的数据";
        }
    }

    /**
     * 获取统计数据列表（聚合计算）
     */
    @GetMapping("/list")
    public List<RechargeStatsDTO> getStatsList(
            @RequestParam(required = false) String dateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        System.out.println("接收到的参数:");
        System.out.println("dateType: " + dateType);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("productId: " + productId);
        System.out.println("customerId: " + customerId);
        System.out.println("supplierId: " + supplierId);
        System.out.println("status: " + status);
        
        // 处理日期参数
        java.sql.Date start = null;
        java.sql.Date end = null;
        
        if (dateType != null && !dateType.isEmpty()) {
            if ("today".equals(dateType)) {
                // 当日数据
                start = new java.sql.Date(System.currentTimeMillis());
                end = new java.sql.Date(System.currentTimeMillis());
            } else if ("range".equals(dateType) && startDate != null && endDate != null) {
                // 日期范围
                try {
                    start = java.sql.Date.valueOf(startDate);
                    end = java.sql.Date.valueOf(endDate);
                } catch (Exception e) {
                    // 日期格式错误，忽略日期条件
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("准备调用聚合查询方法");
        try {
            // 调用聚合查询方法
            List<RechargeStats> statsList = rechargeStatsRepository.findAggregatedStats(
                    productId,
                    customerId,
                    supplierId,
                    status,
                    start,
                    end
            );
            
            System.out.println("聚合查询结果数量: " + statsList.size());
            
            // 转换为DTO
            return statsList.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("聚合查询出错:");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取每日充值汇总数据
     */
    @GetMapping("/daily")
    public List<RechargeStatsDTO> getDailyStatsList(
            @RequestParam(required = false) String dateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        System.out.println("接收到的每日充值汇总参数:");
        System.out.println("dateType: " + dateType);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("productId: " + productId);
        System.out.println("customerId: " + customerId);
        System.out.println("supplierId: " + supplierId);
        System.out.println("status: " + status);
        
        // 处理日期参数
        java.sql.Date start = null;
        java.sql.Date end = null;
        
        if (dateType != null && !dateType.isEmpty()) {
            if ("today".equals(dateType)) {
                // 当日数据
                start = new java.sql.Date(System.currentTimeMillis());
                end = new java.sql.Date(System.currentTimeMillis());
            } else if ("range".equals(dateType) && startDate != null && endDate != null) {
                // 日期范围
                try {
                    start = java.sql.Date.valueOf(startDate);
                    end = java.sql.Date.valueOf(endDate);
                } catch (Exception e) {
                    // 日期格式错误，忽略日期条件
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("准备调用每日充值汇总查询方法");
        try {
            // 调用每日充值汇总查询方法
            List<RechargeStats> statsList = rechargeStatsRepository.findDailyStats(
                    productId,
                    customerId,
                    supplierId,
                    status,
                    start,
                    end
            );
            
            System.out.println("每日充值汇总查询结果数量: " + statsList.size());
            
            // 转换为DTO
            return statsList.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("每日充值汇总查询出错:");
            e.printStackTrace();
            throw e;
        }
    }

    private RechargeStatsDTO convertToDTO(RechargeStats stats) {
        RechargeStatsDTO dto = new RechargeStatsDTO();
        dto.setId(stats.getId());
        dto.setStatDate(stats.getStatDate());
        dto.setProductId(stats.getProductId());
        dto.setProductName(stats.getProductName());
        dto.setProductFacePrice(stats.getProductFacePrice());
        dto.setCustomerId(stats.getCustomerId());
        dto.setCustomerName(stats.getCustomerName());
        dto.setSupplierId(stats.getSupplierId());
        dto.setSupplierName(stats.getSupplierName());
        dto.setStatus(stats.getStatus());
        dto.setRechargeCount(stats.getRechargeCount());
        dto.setCustomerPrice(stats.getCustomerPrice());
        dto.setCustomerAmount(stats.getCustomerAmount());
        dto.setSupplierPrice(stats.getSupplierPrice());
        dto.setCostAmount(stats.getCostAmount());
        return dto;
    }
}
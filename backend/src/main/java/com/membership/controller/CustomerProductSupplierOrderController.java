package com.membership.controller;

import com.membership.entity.CustomerProductSupplierOrder;
import com.membership.repository.CustomerProductSupplierOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-product-supplier-orders")
public class CustomerProductSupplierOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerProductSupplierOrderController.class);
    
    @Autowired
    private CustomerProductSupplierOrderRepository orderRepository;
    
    @GetMapping("/customer-product/{customerProductId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<?> getSupplierOrdersByCustomerProductId(@PathVariable Long customerProductId) {
        logger.info("获取客户产品的供应商顺序列表，客户产品ID: {}", customerProductId);
        List<CustomerProductSupplierOrder> orders = orderRepository.findByCustomerProductId(customerProductId);
        logger.info("成功获取供应商顺序列表，共{}条", orders.size());
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-product:update')")
    @Transactional
    public ResponseEntity<?> saveSupplierOrders(@RequestBody List<CustomerProductSupplierOrder> orders) {
        logger.info("保存供应商顺序列表，订单数量: {}", orders.size());
        
        if (orders.isEmpty()) {
            logger.warn("未提供任何订单数据");
            return ResponseEntity.badRequest().body("No orders provided");
        }
        
        // 获取customerProductId
        Long customerProductId = orders.get(0).getCustomerProductId();
        logger.debug("客户产品ID: {}", customerProductId);
        
        // 删除现有的顺序
        logger.debug("删除现有的供应商顺序记录");
        orderRepository.deleteByCustomerProductId(customerProductId);
        
        // 保存新的顺序
        logger.debug("保存新的供应商顺序记录");
        List<CustomerProductSupplierOrder> savedOrders = orderRepository.saveAll(orders);
        
        logger.info("供应商顺序保存成功，共{}条记录", savedOrders.size());
        return ResponseEntity.ok(savedOrders);
    }
    
    @DeleteMapping("/customer-product/{customerProductId}")
    @PreAuthorize("hasAuthority('customer-product:update')")
    @Transactional
    public ResponseEntity<?> deleteSupplierOrders(@PathVariable Long customerProductId) {
        logger.info("删除客户产品的供应商顺序，客户产品ID: {}", customerProductId);
        orderRepository.deleteByCustomerProductId(customerProductId);
        logger.info("供应商顺序删除成功，客户产品ID: {}", customerProductId);
        return ResponseEntity.ok("Supplier orders deleted successfully");
    }
}

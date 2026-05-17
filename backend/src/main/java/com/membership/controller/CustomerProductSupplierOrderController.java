package com.membership.controller;

import com.membership.entity.CustomerProductSupplierOrder;
import com.membership.repository.CustomerProductSupplierOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-product-supplier-orders")
public class CustomerProductSupplierOrderController {
    
    @Autowired
    private CustomerProductSupplierOrderRepository orderRepository;
    
    @GetMapping("/customer-product/{customerProductId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<?> getSupplierOrdersByCustomerProductId(@PathVariable Long customerProductId) {
        List<CustomerProductSupplierOrder> orders = orderRepository.findByCustomerProductId(customerProductId);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-product:update')")
    @Transactional
    public ResponseEntity<?> saveSupplierOrders(@RequestBody List<CustomerProductSupplierOrder> orders) {
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body("No orders provided");
        }
        
        // 获取customerProductId
        Long customerProductId = orders.get(0).getCustomerProductId();
        
        // 删除现有的顺序
        orderRepository.deleteByCustomerProductId(customerProductId);
        
        // 保存新的顺序
        List<CustomerProductSupplierOrder> savedOrders = orderRepository.saveAll(orders);
        
        return ResponseEntity.ok(savedOrders);
    }
    
    @DeleteMapping("/customer-product/{customerProductId}")
    @PreAuthorize("hasAuthority('customer-product:update')")
    @Transactional
    public ResponseEntity<?> deleteSupplierOrders(@PathVariable Long customerProductId) {
        orderRepository.deleteByCustomerProductId(customerProductId);
        return ResponseEntity.ok("Supplier orders deleted successfully");
    }
}

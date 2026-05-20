package com.membership.external.controller;

import com.membership.core.entity.Customer;
import com.membership.core.entity.CustomerBalance;
import com.membership.core.repository.CustomerBalanceRepository;
import com.membership.core.repository.CustomerRepository;
import com.membership.external.dto.OpenCustomerDTO;
import com.membership.external.dto.OpenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/external/v1/customers")
public class OpenCustomerController {

    private static final Logger logger = LoggerFactory.getLogger(OpenCustomerController.class);

    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;

    @Autowired
    public OpenCustomerController(CustomerRepository customerRepository, 
                                  CustomerBalanceRepository customerBalanceRepository) {
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @GetMapping("/{id}")
    public OpenResponse<OpenCustomerDTO> getCustomer(@PathVariable Long id) {
        logger.info("查询客户信息 - 客户ID: {}", id);
        
        return customerRepository.findById(id)
            .map(customer -> {
                OpenCustomerDTO dto = convertToDTO(customer);
                logger.info("查询成功 - 客户ID: {}, 客户名称: {}", id, customer.getName());
                return OpenResponse.success(dto);
            })
            .orElseGet(() -> {
                logger.warn("客户不存在 - 客户ID: {}", id);
                return OpenResponse.error("客户不存在");
            });
    }

    @GetMapping
    public OpenResponse<List<OpenCustomerDTO>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("查询客户列表 - 页码: {}, 每页大小: {}", page, size);
        
        List<OpenCustomerDTO> customers = customerRepository.findAll()
            .stream()
            .skip((long) page * size)
            .limit(size)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        logger.info("查询成功 - 返回客户数量: {}", customers.size());
        return OpenResponse.success(customers);
    }

    private OpenCustomerDTO convertToDTO(Customer customer) {
        OpenCustomerDTO dto = new OpenCustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setPhone(null);
        dto.setEmail(null);
        dto.setAddress(null);
        dto.setCreatedAt(customer.getCreatedAt());
        
        // 查询余额信息
        CustomerBalance balance = customerBalanceRepository.findByCustomerId(customer.getId())
            .orElse(null);
        
        if (balance != null) {
            dto.setPaymentTotal(balance.getPaymentAccountTotal() != null ? balance.getPaymentAccountTotal() : BigDecimal.ZERO);
            dto.setCreditTotal(balance.getCreditAccountTotal() != null ? balance.getCreditAccountTotal() : BigDecimal.ZERO);
            
            BigDecimal paymentConsumed = balance.getPaymentAccountConsumed() != null ? balance.getPaymentAccountConsumed() : BigDecimal.ZERO;
            BigDecimal creditConsumed = balance.getCreditAccountConsumed() != null ? balance.getCreditAccountConsumed() : BigDecimal.ZERO;
            
            dto.setPaymentBalance(dto.getPaymentTotal().subtract(paymentConsumed));
            dto.setCreditBalance(dto.getCreditTotal().subtract(creditConsumed));
        } else {
            dto.setPaymentTotal(BigDecimal.ZERO);
            dto.setCreditTotal(BigDecimal.ZERO);
            dto.setPaymentBalance(BigDecimal.ZERO);
            dto.setCreditBalance(BigDecimal.ZERO);
        }
        
        return dto;
    }
}
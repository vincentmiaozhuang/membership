package com.membership.controller;

import com.membership.dto.CustomerPaymentDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Customer;
import com.membership.entity.CustomerBalance;
import com.membership.entity.CustomerPayment;
import com.membership.repository.CustomerBalanceRepository;
import com.membership.repository.CustomerPaymentRepository;
import com.membership.repository.CustomerRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/customer-payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerPaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerPaymentController.class);
    
    @Autowired
    private CustomerPaymentRepository customerPaymentRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerBalanceRepository customerBalanceRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<List<CustomerPaymentDTO>> getAllCustomerPayments() {
        logger.info("获取所有客户付款记录");
        List<CustomerPayment> customerPayments = customerPaymentRepository.findAll();
        List<CustomerPaymentDTO> customerPaymentDTOs = customerPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取客户付款记录，共{}条", customerPaymentDTOs.size());
        return ResponseEntity.ok(customerPaymentDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<CustomerPaymentDTO> getCustomerPaymentById(@PathVariable Long id) {
        logger.info("获取客户付款记录详情，ID: {}", id);
        CustomerPayment customerPayment = customerPaymentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户付款记录不存在，ID: {}", id);
                    return new RuntimeException("CustomerPayment not found");
                });
        logger.info("成功获取客户付款记录，ID: {}, 客户ID: {}", id, customerPayment.getCustomer().getId());
        return ResponseEntity.ok(convertToDTO(customerPayment));
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<List<CustomerPaymentDTO>> getCustomerPaymentsByCustomerId(@PathVariable Long customerId) {
        logger.info("获取客户[{}]的付款记录列表", customerId);
        List<CustomerPayment> customerPayments = customerPaymentRepository.findByCustomerId(customerId);
        List<CustomerPaymentDTO> customerPaymentDTOs = customerPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取客户[{}]的付款记录，共{}条", customerId, customerPaymentDTOs.size());
        return ResponseEntity.ok(customerPaymentDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-payment:create')")
    public ResponseEntity<CustomerPaymentDTO> createCustomerPayment(@RequestBody CustomerPaymentDTO customerPaymentDTO) {
        logger.info("创建客户付款记录，客户ID: {}, 付款金额: {}", customerPaymentDTO.getCustomerId(), customerPaymentDTO.getPaymentAmount());
        Customer customer = customerRepository.findById(customerPaymentDTO.getCustomerId())
                .orElseThrow(() -> {
                    logger.error("客户不存在，ID: {}", customerPaymentDTO.getCustomerId());
                    return new RuntimeException("Customer not found");
                });
        
        CustomerPayment customerPayment = new CustomerPayment();
        customerPayment.setCustomer(customer);
        customerPayment.setPaymentAmount(customerPaymentDTO.getPaymentAmount());
        customerPayment.setCreditAmount(customerPaymentDTO.getCreditAmount());
        customerPayment.setFinancialScreenshot(customerPaymentDTO.getFinancialScreenshot());
        customerPayment.setOperator(getCurrentUserId());
        
        CustomerPayment savedCustomerPayment = customerPaymentRepository.save(customerPayment);
        logger.info("客户付款记录创建成功，ID: {}, 客户ID: {}, 付款金额: {}", 
                savedCustomerPayment.getId(), customer.getId(), savedCustomerPayment.getPaymentAmount());
        
        // 更新客户付款账户总额
        updateCustomerPaymentAccountTotal(customerPaymentDTO.getCustomerId());
        
        return ResponseEntity.ok(convertToDTO(savedCustomerPayment));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:update')")
    public ResponseEntity<CustomerPaymentDTO> updateCustomerPayment(@PathVariable Long id, @RequestBody CustomerPaymentDTO customerPaymentDTO) {
        logger.info("更新客户付款记录，ID: {}, 客户ID: {}, 付款金额: {}", 
                id, customerPaymentDTO.getCustomerId(), customerPaymentDTO.getPaymentAmount());
        CustomerPayment customerPayment = customerPaymentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户付款记录不存在，ID: {}", id);
                    return new RuntimeException("CustomerPayment not found");
                });
        
        Customer customer = customerRepository.findById(customerPaymentDTO.getCustomerId())
                .orElseThrow(() -> {
                    logger.error("客户不存在，ID: {}", customerPaymentDTO.getCustomerId());
                    return new RuntimeException("Customer not found");
                });
        
        customerPayment.setCustomer(customer);
        customerPayment.setPaymentAmount(customerPaymentDTO.getPaymentAmount());
        customerPayment.setCreditAmount(customerPaymentDTO.getCreditAmount());
        customerPayment.setFinancialScreenshot(customerPaymentDTO.getFinancialScreenshot());
        customerPayment.setOperator(getCurrentUserId());
        
        CustomerPayment updatedCustomerPayment = customerPaymentRepository.save(customerPayment);
        logger.info("客户付款记录更新成功，ID: {}, 客户ID: {}, 付款金额: {}", 
                updatedCustomerPayment.getId(), customer.getId(), updatedCustomerPayment.getPaymentAmount());
        
        // 更新客户付款账户总额
        updateCustomerPaymentAccountTotal(customerPaymentDTO.getCustomerId());
        
        return ResponseEntity.ok(convertToDTO(updatedCustomerPayment));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:delete')")
    public ResponseEntity<?> deleteCustomerPayment(@PathVariable Long id) {
        logger.info("删除客户付款记录，ID: {}", id);
        // 获取要删除的付款记录的客户ID
        CustomerPayment customerPayment = customerPaymentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户付款记录不存在，ID: {}", id);
                    return new RuntimeException("CustomerPayment not found");
                });
        Long customerId = customerPayment.getCustomer().getId();
        BigDecimal paymentAmount = customerPayment.getPaymentAmount();
        
        customerPaymentRepository.deleteById(id);
        logger.info("客户付款记录删除成功，ID: {}, 客户ID: {}, 付款金额: {}", id, customerId, paymentAmount);
        
        // 更新客户付款账户总额
        updateCustomerPaymentAccountTotal(customerId);
        
        return ResponseEntity.ok(new MessageResponse("CustomerPayment deleted successfully"));
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
    
    /**
     * 更新客户付款账户总额
     * 根据该客户所有已有的付款记录进行加和，重新生成该客户付款账户的总额
     */
    private void updateCustomerPaymentAccountTotal(Long customerId) {
        // 计算该客户所有付款记录的总和
        BigDecimal totalPayment = customerPaymentRepository.findByCustomerId(customerId)
                .stream()
                .map(payment -> payment.getPaymentAmount() != null ? payment.getPaymentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 查找或创建客户余额记录
        Optional<CustomerBalance> existingBalance = customerBalanceRepository.findByCustomerId(customerId);
        CustomerBalance customerBalance = existingBalance.orElse(new CustomerBalance());
        
        // 设置付款账户总额
        customerBalance.setPaymentAccountTotal(totalPayment);
        
        // 如果是新创建的余额记录，需要设置客户关联和其他默认值
        if (!existingBalance.isPresent()) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            customerBalance.setCustomer(customer);
            customerBalance.setPaymentAccountConsumed(BigDecimal.ZERO);
            customerBalance.setPaymentAccountBalance(totalPayment);
            customerBalance.setCreditAccountTotal(BigDecimal.ZERO);
            customerBalance.setCreditAccountConsumed(BigDecimal.ZERO);
            customerBalance.setCreditAccountBalance(BigDecimal.ZERO);
            customerBalance.setTotalAccountAmount(totalPayment);
            customerBalance.setTotalAccountConsumed(BigDecimal.ZERO);
            customerBalance.setTotalAccountBalance(totalPayment);
            customerBalance.setAlertThreshold(BigDecimal.ZERO);
            customerBalance.setEnabled(true);
            customerBalance.setVipPassEnabled(false);
        } else {
            // 更新现有记录的付款账户余额（总额 - 已消耗）
            BigDecimal consumed = customerBalance.getPaymentAccountConsumed() != null ? customerBalance.getPaymentAccountConsumed() : BigDecimal.ZERO;
            customerBalance.setPaymentAccountBalance(totalPayment.subtract(consumed));
            
            // 更新总账户金额
            BigDecimal creditTotal = customerBalance.getCreditAccountTotal() != null ? customerBalance.getCreditAccountTotal() : BigDecimal.ZERO;
            customerBalance.setTotalAccountAmount(totalPayment.add(creditTotal));
            
            BigDecimal totalConsumed = customerBalance.getTotalAccountConsumed() != null ? customerBalance.getTotalAccountConsumed() : BigDecimal.ZERO;
            customerBalance.setTotalAccountBalance(customerBalance.getTotalAccountAmount().subtract(totalConsumed));
        }
        
        // 保存客户余额记录
        customerBalanceRepository.save(customerBalance);
    }
    
    private CustomerPaymentDTO convertToDTO(CustomerPayment customerPayment) {
        CustomerPaymentDTO dto = new CustomerPaymentDTO();
        dto.setId(customerPayment.getId());
        dto.setCustomerId(customerPayment.getCustomer().getId());
        dto.setCustomerName(customerPayment.getCustomer().getName());
        dto.setPaymentAmount(customerPayment.getPaymentAmount());
        dto.setCreditAmount(customerPayment.getCreditAmount());
        dto.setFinancialScreenshot(customerPayment.getFinancialScreenshot());
        dto.setOperator(customerPayment.getOperator());
        dto.setCreatedAt(customerPayment.getCreatedAt());
        dto.setUpdatedAt(customerPayment.getUpdatedAt());
        return dto;
    }
}

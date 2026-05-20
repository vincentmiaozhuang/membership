package com.membership.controller;

import com.membership.dto.CustomerBalanceDTO;
import com.membership.dto.MessageResponse;
import com.membership.core.entity.Customer;
import com.membership.core.entity.CustomerBalance;
import com.membership.core.repository.CustomerBalanceRepository;
import com.membership.core.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer-balances")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerBalanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerBalanceController.class);
    
    @Autowired
    private CustomerBalanceRepository customerBalanceRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-balance:read')")
    public ResponseEntity<List<CustomerBalanceDTO>> getAllCustomerBalances() {
        logger.info("获取所有客户余额记录");
        List<CustomerBalance> balances = customerBalanceRepository.findAll();
        List<CustomerBalanceDTO> balanceDTOs = balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取客户余额记录，共{}条", balanceDTOs.size());
        return ResponseEntity.ok(balanceDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:read')")
    public ResponseEntity<CustomerBalanceDTO> getCustomerBalanceById(@PathVariable Long id) {
        logger.info("获取客户余额详情，ID: {}", id);
        CustomerBalance balance = customerBalanceRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户余额记录不存在，ID: {}", id);
                    return new RuntimeException("Customer balance not found");
                });
        logger.info("成功获取客户余额记录，ID: {}, 客户ID: {}", id, balance.getCustomer().getId());
        return ResponseEntity.ok(convertToDTO(balance));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerBalanceDTO> getCustomerBalanceByCustomerId(@PathVariable Long customerId) {
        logger.info("获取客户[{}]的余额记录", customerId);
        CustomerBalance balance = customerBalanceRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    logger.info("客户[{}]没有余额记录，创建默认余额记录", customerId);
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> {
                                logger.error("客户不存在，ID: {}", customerId);
                                return new RuntimeException("Customer not found");
                            });
                    CustomerBalance newBalance = new CustomerBalance();
                    newBalance.setCustomer(customer);
                    newBalance.setPaymentAccountTotal(java.math.BigDecimal.ZERO);
                    newBalance.setPaymentAccountConsumed(java.math.BigDecimal.ZERO);
                    newBalance.setPaymentAccountBalance(java.math.BigDecimal.ZERO);
                    newBalance.setCreditAccountTotal(java.math.BigDecimal.ZERO);
                    newBalance.setCreditAccountConsumed(java.math.BigDecimal.ZERO);
                    newBalance.setCreditAccountBalance(java.math.BigDecimal.ZERO);
                    newBalance.setTotalAccountAmount(java.math.BigDecimal.ZERO);
                    newBalance.setTotalAccountConsumed(java.math.BigDecimal.ZERO);
                    newBalance.setTotalAccountBalance(java.math.BigDecimal.ZERO);
                    newBalance.setAlertThreshold(java.math.BigDecimal.ZERO);
                    newBalance.setEnabled(true);
                    CustomerBalance saved = customerBalanceRepository.save(newBalance);
                    logger.info("客户[{}]默认余额记录创建成功，余额ID: {}", customerId, saved.getId());
                    return saved;
                });
        logger.info("成功获取客户[{}]的余额记录，余额ID: {}", customerId, balance.getId());
        return ResponseEntity.ok(convertToDTO(balance));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-balance:create')")
    public ResponseEntity<?> createCustomerBalance(@RequestBody CustomerBalanceDTO customerBalanceDTO) {
        logger.info("创建客户余额记录，客户ID: {}", customerBalanceDTO.getCustomerId());
        Customer customer = customerRepository.findById(customerBalanceDTO.getCustomerId())
                .orElseThrow(() -> {
                    logger.error("客户不存在，ID: {}", customerBalanceDTO.getCustomerId());
                    return new RuntimeException("Customer not found");
                });
        
        if (customerBalanceRepository.findByCustomerId(customerBalanceDTO.getCustomerId()).isPresent()) {
            logger.warn("客户[{}]余额记录已存在，创建失败", customerBalanceDTO.getCustomerId());
            return ResponseEntity.badRequest().body(new MessageResponse("该客户余额记录已存在"));
        }
        
        CustomerBalance balance = new CustomerBalance();
        balance.setCustomer(customer);
        balance.setPaymentAccountTotal(customerBalanceDTO.getPaymentAccountTotal() != null ? customerBalanceDTO.getPaymentAccountTotal() : BigDecimal.ZERO);
        balance.setPaymentAccountConsumed(customerBalanceDTO.getPaymentAccountConsumed() != null ? customerBalanceDTO.getPaymentAccountConsumed() : BigDecimal.ZERO);
        balance.setPaymentAccountBalance(customerBalanceDTO.getPaymentAccountBalance() != null ? customerBalanceDTO.getPaymentAccountBalance() : BigDecimal.ZERO);
        balance.setCreditAccountTotal(customerBalanceDTO.getCreditAccountTotal() != null ? customerBalanceDTO.getCreditAccountTotal() : BigDecimal.ZERO);
        balance.setCreditAccountConsumed(customerBalanceDTO.getCreditAccountConsumed() != null ? customerBalanceDTO.getCreditAccountConsumed() : BigDecimal.ZERO);
        balance.setCreditAccountBalance(customerBalanceDTO.getCreditAccountBalance() != null ? customerBalanceDTO.getCreditAccountBalance() : BigDecimal.ZERO);
        balance.setTotalAccountAmount(customerBalanceDTO.getTotalAccountAmount() != null ? customerBalanceDTO.getTotalAccountAmount() : BigDecimal.ZERO);
        balance.setTotalAccountConsumed(customerBalanceDTO.getTotalAccountConsumed() != null ? customerBalanceDTO.getTotalAccountConsumed() : BigDecimal.ZERO);
        balance.setTotalAccountBalance(customerBalanceDTO.getTotalAccountBalance() != null ? customerBalanceDTO.getTotalAccountBalance() : BigDecimal.ZERO);
        balance.setAlertThreshold(customerBalanceDTO.getAlertThreshold() != null ? customerBalanceDTO.getAlertThreshold() : BigDecimal.ZERO);
        balance.setEnabled(customerBalanceDTO.getEnabled() != null ? customerBalanceDTO.getEnabled() : true);
        
        CustomerBalance savedBalance = customerBalanceRepository.save(balance);
        logger.info("客户余额记录创建成功，ID: {}, 客户ID: {}", savedBalance.getId(), customer.getId());
        return ResponseEntity.ok(convertToDTO(savedBalance));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:update')")
    public ResponseEntity<?> updateCustomerBalance(@PathVariable Long id, @RequestBody CustomerBalanceDTO customerBalanceDTO) {
        logger.info("更新客户余额记录，ID: {}", id);
        CustomerBalance balance = customerBalanceRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户余额记录不存在，ID: {}", id);
                    return new RuntimeException("Customer balance not found");
                });
        
        // 更新付款账户总额（如果提供）
        if (customerBalanceDTO.getPaymentAccountTotal() != null) {
            logger.debug("更新付款账户总额: {} -> {}", balance.getPaymentAccountTotal(), customerBalanceDTO.getPaymentAccountTotal());
            balance.setPaymentAccountTotal(customerBalanceDTO.getPaymentAccountTotal());
        }
        // 更新付款账户已消费金额（如果提供）
        if (customerBalanceDTO.getPaymentAccountConsumed() != null) {
            logger.debug("更新付款账户已消费金额: {} -> {}", balance.getPaymentAccountConsumed(), customerBalanceDTO.getPaymentAccountConsumed());
            balance.setPaymentAccountConsumed(customerBalanceDTO.getPaymentAccountConsumed());
        }
        
        // 更新授信账户总额（如果提供）
        if (customerBalanceDTO.getCreditAccountTotal() != null) {
            logger.debug("更新授信账户总额: {} -> {}", balance.getCreditAccountTotal(), customerBalanceDTO.getCreditAccountTotal());
            balance.setCreditAccountTotal(customerBalanceDTO.getCreditAccountTotal());
        }
        // 更新授信账户已消费金额（如果提供）
        if (customerBalanceDTO.getCreditAccountConsumed() != null) {
            logger.debug("更新授信账户已消费金额: {} -> {}", balance.getCreditAccountConsumed(), customerBalanceDTO.getCreditAccountConsumed());
            balance.setCreditAccountConsumed(customerBalanceDTO.getCreditAccountConsumed());
        }
        
        // 根据公式自动计算各字段
        calculateBalanceFields(balance);
        logger.debug("余额字段计算完成: 付款余额={}, 授信余额={}, 总计总额={}, 总计余额={}", 
                balance.getPaymentAccountBalance(), balance.getCreditAccountBalance(),
                balance.getTotalAccountAmount(), balance.getTotalAccountBalance());
        
        // 更新其他字段
        if (customerBalanceDTO.getAlertThreshold() != null) {
            balance.setAlertThreshold(customerBalanceDTO.getAlertThreshold());
        }
        if (customerBalanceDTO.getEnabled() != null) {
            balance.setEnabled(customerBalanceDTO.getEnabled());
        }
        if (customerBalanceDTO.getVipPassEnabled() != null) {
            balance.setVipPassEnabled(customerBalanceDTO.getVipPassEnabled());
        }
        
        CustomerBalance updatedBalance = customerBalanceRepository.save(balance);
        logger.info("客户余额记录更新成功，ID: {}, 客户ID: {}", updatedBalance.getId(), updatedBalance.getCustomer().getId());
        return ResponseEntity.ok(convertToDTO(updatedBalance));
    }
    
    /**
     * 根据公式计算余额字段
     * 1. 付款账户余额 = 付款账户总额 - 付款账户已消费金额
     * 2. 授信账户余额 = 授信账户总额 - 授信账户已消费金额
     * 3. 账户总计总额 = 付款账户总额 + 授信账户总额
     * 4. 账户总计已消费金额 = 付款账户已消费金额 + 授信账户已消费金额
     * 5. 账户总计余额 = 付款账户余额 + 授信账户余额
     */
    private void calculateBalanceFields(CustomerBalance balance) {
        // 获取各字段值，为空则默认为0
        BigDecimal paymentTotal = balance.getPaymentAccountTotal() != null ? balance.getPaymentAccountTotal() : BigDecimal.ZERO;
        BigDecimal paymentConsumed = balance.getPaymentAccountConsumed() != null ? balance.getPaymentAccountConsumed() : BigDecimal.ZERO;
        BigDecimal creditTotal = balance.getCreditAccountTotal() != null ? balance.getCreditAccountTotal() : BigDecimal.ZERO;
        BigDecimal creditConsumed = balance.getCreditAccountConsumed() != null ? balance.getCreditAccountConsumed() : BigDecimal.ZERO;
        
        // 1. 付款账户余额 = 付款账户总额 - 付款账户已消费金额
        BigDecimal paymentBalance = paymentTotal.subtract(paymentConsumed);
        balance.setPaymentAccountBalance(paymentBalance);
        
        // 2. 授信账户余额 = 授信账户总额 - 授信账户已消费金额
        BigDecimal creditBalance = creditTotal.subtract(creditConsumed);
        balance.setCreditAccountBalance(creditBalance);
        
        // 3. 账户总计总额 = 付款账户总额 + 授信账户总额
        BigDecimal totalAmount = paymentTotal.add(creditTotal);
        balance.setTotalAccountAmount(totalAmount);
        
        // 4. 账户总计已消费金额 = 付款账户已消费金额 + 授信账户已消费金额
        BigDecimal totalConsumed = paymentConsumed.add(creditConsumed);
        balance.setTotalAccountConsumed(totalConsumed);
        
        // 5. 账户总计余额 = 付款账户余额 + 授信账户余额
        BigDecimal totalBalance = paymentBalance.add(creditBalance);
        balance.setTotalAccountBalance(totalBalance);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:delete')")
    public ResponseEntity<?> deleteCustomerBalance(@PathVariable Long id) {
        logger.info("删除客户余额记录，ID: {}", id);
        customerBalanceRepository.deleteById(id);
        logger.info("客户余额记录删除成功，ID: {}", id);
        return ResponseEntity.ok(new MessageResponse("Customer balance deleted successfully"));
    }
    
    private CustomerBalanceDTO convertToDTO(CustomerBalance balance) {
        CustomerBalanceDTO dto = new CustomerBalanceDTO();
        dto.setId(balance.getId());
        dto.setCustomerId(balance.getCustomer().getId());
        dto.setCustomerName(balance.getCustomer().getName());
        dto.setPaymentAccountTotal(balance.getPaymentAccountTotal());
        dto.setPaymentAccountConsumed(balance.getPaymentAccountConsumed());
        dto.setPaymentAccountBalance(balance.getPaymentAccountBalance());
        dto.setCreditAccountTotal(balance.getCreditAccountTotal());
        dto.setCreditAccountConsumed(balance.getCreditAccountConsumed());
        dto.setCreditAccountBalance(balance.getCreditAccountBalance());
        dto.setTotalAccountAmount(balance.getTotalAccountAmount());
        dto.setTotalAccountConsumed(balance.getTotalAccountConsumed());
        dto.setTotalAccountBalance(balance.getTotalAccountBalance());
        dto.setAlertThreshold(balance.getAlertThreshold());
        dto.setEnabled(balance.getEnabled());
        dto.setVipPassEnabled(balance.getVipPassEnabled());
        dto.setCreatedAt(balance.getCreatedAt());
        dto.setUpdatedAt(balance.getUpdatedAt());
        return dto;
    }
}

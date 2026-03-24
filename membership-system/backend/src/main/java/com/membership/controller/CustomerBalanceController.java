package com.membership.controller;

import com.membership.dto.CustomerBalanceDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Customer;
import com.membership.entity.CustomerBalance;
import com.membership.repository.CustomerBalanceRepository;
import com.membership.repository.CustomerRepository;
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
    
    @Autowired
    private CustomerBalanceRepository customerBalanceRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-balance:read')")
    public ResponseEntity<List<CustomerBalanceDTO>> getAllCustomerBalances() {
        List<CustomerBalance> balances = customerBalanceRepository.findAll();
        List<CustomerBalanceDTO> balanceDTOs = balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(balanceDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:read')")
    public ResponseEntity<CustomerBalanceDTO> getCustomerBalanceById(@PathVariable Long id) {
        CustomerBalance balance = customerBalanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer balance not found"));
        return ResponseEntity.ok(convertToDTO(balance));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerBalanceDTO> getCustomerBalanceByCustomerId(@PathVariable Long customerId) {
        CustomerBalance balance = customerBalanceRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    // 如果客户没有余额记录，创建一个默认的余额记录
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));
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
                    return customerBalanceRepository.save(newBalance);
                });
        return ResponseEntity.ok(convertToDTO(balance));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-balance:create')")
    public ResponseEntity<?> createCustomerBalance(@RequestBody CustomerBalanceDTO customerBalanceDTO) {
        Customer customer = customerRepository.findById(customerBalanceDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customerBalanceRepository.findByCustomerId(customerBalanceDTO.getCustomerId()).isPresent()) {
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
        return ResponseEntity.ok(convertToDTO(savedBalance));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:update')")
    public ResponseEntity<?> updateCustomerBalance(@PathVariable Long id, @RequestBody CustomerBalanceDTO customerBalanceDTO) {
        CustomerBalance balance = customerBalanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer balance not found"));
        
        if (customerBalanceDTO.getPaymentAccountTotal() != null) {
            balance.setPaymentAccountTotal(customerBalanceDTO.getPaymentAccountTotal());
        }
        if (customerBalanceDTO.getPaymentAccountConsumed() != null) {
            balance.setPaymentAccountConsumed(customerBalanceDTO.getPaymentAccountConsumed());
        }
        if (customerBalanceDTO.getPaymentAccountBalance() != null) {
            balance.setPaymentAccountBalance(customerBalanceDTO.getPaymentAccountBalance());
        }
        if (customerBalanceDTO.getCreditAccountTotal() != null) {
            balance.setCreditAccountTotal(customerBalanceDTO.getCreditAccountTotal());
        }
        if (customerBalanceDTO.getCreditAccountConsumed() != null) {
            balance.setCreditAccountConsumed(customerBalanceDTO.getCreditAccountConsumed());
        }
        if (customerBalanceDTO.getCreditAccountBalance() != null) {
            balance.setCreditAccountBalance(customerBalanceDTO.getCreditAccountBalance());
        }
        if (customerBalanceDTO.getTotalAccountAmount() != null) {
            balance.setTotalAccountAmount(customerBalanceDTO.getTotalAccountAmount());
        }
        if (customerBalanceDTO.getTotalAccountConsumed() != null) {
            balance.setTotalAccountConsumed(customerBalanceDTO.getTotalAccountConsumed());
        }
        if (customerBalanceDTO.getTotalAccountBalance() != null) {
            balance.setTotalAccountBalance(customerBalanceDTO.getTotalAccountBalance());
        }
        if (customerBalanceDTO.getAlertThreshold() != null) {
            balance.setAlertThreshold(customerBalanceDTO.getAlertThreshold());
        }
        if (customerBalanceDTO.getEnabled() != null) {
            balance.setEnabled(customerBalanceDTO.getEnabled());
        }
        
        CustomerBalance updatedBalance = customerBalanceRepository.save(balance);
        return ResponseEntity.ok(convertToDTO(updatedBalance));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-balance:delete')")
    public ResponseEntity<?> deleteCustomerBalance(@PathVariable Long id) {
        customerBalanceRepository.deleteById(id);
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
        dto.setCreatedAt(balance.getCreatedAt());
        dto.setUpdatedAt(balance.getUpdatedAt());
        return dto;
    }
}

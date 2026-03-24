package com.membership.controller;

import com.membership.dto.CustomerPaymentDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Customer;
import com.membership.entity.CustomerPayment;
import com.membership.repository.CustomerPaymentRepository;
import com.membership.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/customer-payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerPaymentController {
    
    @Autowired
    private CustomerPaymentRepository customerPaymentRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<List<CustomerPaymentDTO>> getAllCustomerPayments() {
        List<CustomerPayment> customerPayments = customerPaymentRepository.findAll();
        List<CustomerPaymentDTO> customerPaymentDTOs = customerPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerPaymentDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<CustomerPaymentDTO> getCustomerPaymentById(@PathVariable Long id) {
        CustomerPayment customerPayment = customerPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerPayment not found"));
        return ResponseEntity.ok(convertToDTO(customerPayment));
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('customer-payment:read')")
    public ResponseEntity<List<CustomerPaymentDTO>> getCustomerPaymentsByCustomerId(@PathVariable Long customerId) {
        List<CustomerPayment> customerPayments = customerPaymentRepository.findByCustomerId(customerId);
        List<CustomerPaymentDTO> customerPaymentDTOs = customerPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerPaymentDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-payment:create')")
    public ResponseEntity<CustomerPaymentDTO> createCustomerPayment(@RequestBody CustomerPaymentDTO customerPaymentDTO) {
        Customer customer = customerRepository.findById(customerPaymentDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        CustomerPayment customerPayment = new CustomerPayment();
        customerPayment.setCustomer(customer);
        customerPayment.setPaymentAmount(customerPaymentDTO.getPaymentAmount());
        customerPayment.setCreditAmount(customerPaymentDTO.getCreditAmount());
        customerPayment.setFinancialScreenshot(customerPaymentDTO.getFinancialScreenshot());
        customerPayment.setOperator(getCurrentUserId());
        
        CustomerPayment savedCustomerPayment = customerPaymentRepository.save(customerPayment);
        return ResponseEntity.ok(convertToDTO(savedCustomerPayment));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:update')")
    public ResponseEntity<CustomerPaymentDTO> updateCustomerPayment(@PathVariable Long id, @RequestBody CustomerPaymentDTO customerPaymentDTO) {
        CustomerPayment customerPayment = customerPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerPayment not found"));
        
        Customer customer = customerRepository.findById(customerPaymentDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customerPayment.setCustomer(customer);
        customerPayment.setPaymentAmount(customerPaymentDTO.getPaymentAmount());
        customerPayment.setCreditAmount(customerPaymentDTO.getCreditAmount());
        customerPayment.setFinancialScreenshot(customerPaymentDTO.getFinancialScreenshot());
        customerPayment.setOperator(getCurrentUserId());
        
        CustomerPayment updatedCustomerPayment = customerPaymentRepository.save(customerPayment);
        return ResponseEntity.ok(convertToDTO(updatedCustomerPayment));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-payment:delete')")
    public ResponseEntity<?> deleteCustomerPayment(@PathVariable Long id) {
        customerPaymentRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("CustomerPayment deleted successfully"));
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
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

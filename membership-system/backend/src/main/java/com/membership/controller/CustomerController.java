package com.membership.controller;

import com.membership.dto.CustomerDTO;
import com.membership.dto.CustomerProductDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Customer;
import com.membership.entity.CustomerProduct;
import com.membership.entity.Product;
import com.membership.repository.CustomerProductRepository;
import com.membership.repository.CustomerRepository;
import com.membership.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerProductRepository customerProductRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer:read')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOs = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:read')")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(convertToDTO(customer));
    }
    
    @GetMapping("/enabled")
    //获取可用的客户，注释掉就是为了在充值记录管理中能够有权限获取
    //@PreAuthorize("hasAuthority('customer:read')")
    public ResponseEntity<List<CustomerDTO>> getEnabledCustomers() {
        List<Customer> customers = customerRepository.findByEnabledTrue();
        List<CustomerDTO> customerDTOs = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDTOs);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('customer:read')")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String name) {
        List<Customer> customers = customerRepository.findByNameContaining(name);
        List<CustomerDTO> customerDTOs = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer:create')")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO) {
        // 生成16位客户代码
        String customerCode = generateRandomCode(16);
        // 生成16位客户秘钥
        String customerSecret = generateRandomCode(16);
        
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setCustomerCode(customerCode);
        customer.setCustomerSecret(customerSecret);
        customer.setIpWhitelist(customerDTO.getIpWhitelist());
        customer.setCooperationStartDate(customerDTO.getCooperationStartDate());
        customer.setEnabled(customerDTO.getEnabled() != null ? customerDTO.getEnabled() : true);
        
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(convertToDTO(savedCustomer));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:update')")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setName(customerDTO.getName());
        customer.setIpWhitelist(customerDTO.getIpWhitelist());
        customer.setCooperationStartDate(customerDTO.getCooperationStartDate());
        customer.setEnabled(customerDTO.getEnabled() != null ? customerDTO.getEnabled() : true);
        
        Customer updatedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(convertToDTO(updatedCustomer));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:delete')")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Customer deleted successfully"));
    }
    
    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setCustomerCode(customer.getCustomerCode());
        dto.setCustomerSecret(customer.getCustomerSecret());
        dto.setIpWhitelist(customer.getIpWhitelist());
        dto.setCooperationStartDate(customer.getCooperationStartDate());
        dto.setEnabled(customer.getEnabled());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
    
    // 生成指定长度的随机字符串
    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
    
    @GetMapping("/{customerId}/products")
    @PreAuthorize("hasAuthority('customer:read')")
    public ResponseEntity<List<CustomerProductDTO>> getCustomerProducts(@PathVariable Long customerId) {
        List<CustomerProduct> customerProducts = customerProductRepository.findByCustomerId(customerId);
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToCustomerProductDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @PostMapping("/{customerId}/products")
    @PreAuthorize("hasAuthority('customer:update')")
    public ResponseEntity<CustomerProductDTO> assignProductToCustomer(@PathVariable Long customerId, @RequestBody CustomerProductDTO customerProductDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Product product = productRepository.findById(customerProductDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        CustomerProduct customerProduct = new CustomerProduct();
        customerProduct.setCustomer(customer);
        customerProduct.setProduct(product);
        customerProduct.setCustomerProductCode(generateRandomCode(16));
        customerProduct.setCustomerPrice(customerProductDTO.getCustomerPrice());
        customerProduct.setStockQuantity(customerProductDTO.getStockQuantity());
        customerProduct.setShipmentQuantity(0);
        customerProduct.setStockAmount(customerProductDTO.getStockAmount());
        customerProduct.setShipmentAmount(java.math.BigDecimal.ZERO);
        customerProduct.setEnabled(true);
        
        CustomerProduct savedCustomerProduct = customerProductRepository.save(customerProduct);
        return ResponseEntity.ok(convertToCustomerProductDTO(savedCustomerProduct));
    }
    
    @DeleteMapping("/{customerId}/products/{customerProductId}")
    @PreAuthorize("hasAuthority('customer:update')")
    public ResponseEntity<?> removeProductFromCustomer(@PathVariable Long customerId, @PathVariable Long customerProductId) {
        customerProductRepository.deleteById(customerProductId);
        return ResponseEntity.ok(new MessageResponse("Product removed successfully"));
    }
    
    private CustomerProductDTO convertToCustomerProductDTO(CustomerProduct customerProduct) {
        CustomerProductDTO dto = new CustomerProductDTO();
        dto.setId(customerProduct.getId());
        dto.setCustomerId(customerProduct.getCustomer().getId());
        dto.setCustomerName(customerProduct.getCustomer().getName());
        dto.setProductId(customerProduct.getProduct().getId());
        dto.setProductName(customerProduct.getProduct().getName());
        dto.setProductCode(customerProduct.getProduct().getProductCode());
        dto.setProductType(customerProduct.getProduct().getType());
        dto.setFaceValue(customerProduct.getProduct().getFaceValue());
        dto.setCustomerProductCode(customerProduct.getCustomerProductCode());
        dto.setCustomerPrice(customerProduct.getCustomerPrice());
        dto.setStockQuantity(customerProduct.getStockQuantity());
        dto.setShipmentQuantity(customerProduct.getShipmentQuantity());
        dto.setStockAmount(customerProduct.getStockAmount());
        dto.setShipmentAmount(customerProduct.getShipmentAmount());
        dto.setEnabled(customerProduct.getEnabled());
        dto.setCreatedAt(customerProduct.getCreatedAt());
        dto.setUpdatedAt(customerProduct.getUpdatedAt());
        return dto;
    }
}

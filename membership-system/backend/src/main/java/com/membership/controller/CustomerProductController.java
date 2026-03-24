package com.membership.controller;

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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer-products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerProductController {
    
    @Autowired
    private CustomerProductRepository customerProductRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getAllCustomerProducts() {
        List<CustomerProduct> customerProducts = customerProductRepository.findAll();
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<CustomerProductDTO> getCustomerProductById(@PathVariable Long id) {
        CustomerProduct customerProduct = customerProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerProduct not found"));
        return ResponseEntity.ok(convertToDTO(customerProduct));
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getCustomerProductsByCustomerId(@PathVariable Long customerId) {
        List<CustomerProduct> customerProducts = customerProductRepository.findByCustomerId(customerId);
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getCustomerProductsByProductId(@PathVariable Long productId) {
        List<CustomerProduct> customerProducts = customerProductRepository.findByProductId(productId);
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-product:create')")
    public ResponseEntity<?> createCustomerProduct(@RequestBody CustomerProductDTO customerProductDTO) {
        Customer customer = customerRepository.findById(customerProductDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Product product = productRepository.findById(customerProductDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 生成32位客户产品代码
        String customerProductCode = generateRandomCode(32);
        
        CustomerProduct customerProduct = new CustomerProduct();
        customerProduct.setCustomer(customer);
        customerProduct.setProduct(product);
        customerProduct.setCustomerProductCode(customerProductCode);
        customerProduct.setCustomerPrice(customerProductDTO.getCustomerPrice());
        customerProduct.setStockQuantity(customerProductDTO.getStockQuantity());
        customerProduct.setShipmentQuantity(customerProductDTO.getShipmentQuantity());
        customerProduct.setStockAmount(customerProductDTO.getStockAmount());
        customerProduct.setShipmentAmount(customerProductDTO.getShipmentAmount());
        customerProduct.setEnabled(customerProductDTO.getEnabled() != null ? customerProductDTO.getEnabled() : true);
        
        CustomerProduct savedCustomerProduct = customerProductRepository.save(customerProduct);
        return ResponseEntity.ok(convertToDTO(savedCustomerProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:update')")
    public ResponseEntity<?> updateCustomerProduct(@PathVariable Long id, @RequestBody CustomerProductDTO customerProductDTO) {
        CustomerProduct customerProduct = customerProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CustomerProduct not found"));
        
        if (customerProductDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(customerProductDTO.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            customerProduct.setCustomer(customer);
        }
        
        if (customerProductDTO.getProductId() != null) {
            Product product = productRepository.findById(customerProductDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            customerProduct.setProduct(product);
        }
        
        customerProduct.setCustomerPrice(customerProductDTO.getCustomerPrice());
        customerProduct.setStockQuantity(customerProductDTO.getStockQuantity());
        customerProduct.setShipmentQuantity(customerProductDTO.getShipmentQuantity());
        customerProduct.setStockAmount(customerProductDTO.getStockAmount());
        customerProduct.setShipmentAmount(customerProductDTO.getShipmentAmount());
        customerProduct.setEnabled(customerProductDTO.getEnabled() != null ? customerProductDTO.getEnabled() : true);
        
        CustomerProduct updatedCustomerProduct = customerProductRepository.save(customerProduct);
        return ResponseEntity.ok(convertToDTO(updatedCustomerProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:delete')")
    public ResponseEntity<?> deleteCustomerProduct(@PathVariable Long id) {
        customerProductRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("CustomerProduct deleted successfully"));
    }
    
    private CustomerProductDTO convertToDTO(CustomerProduct customerProduct) {
        CustomerProductDTO dto = new CustomerProductDTO();
        dto.setId(customerProduct.getId());
        dto.setCustomerId(customerProduct.getCustomer().getId());
        dto.setCustomerName(customerProduct.getCustomer().getName());
        dto.setProductId(customerProduct.getProduct().getId());
        dto.setProductName(customerProduct.getProduct().getName());
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
}
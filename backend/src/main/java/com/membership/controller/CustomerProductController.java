package com.membership.controller;

import com.membership.dto.CustomerProductDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Customer;
import com.membership.entity.CustomerProduct;
import com.membership.entity.Product;
import com.membership.repository.CustomerProductRepository;
import com.membership.repository.CustomerRepository;
import com.membership.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerProductController.class);
    
    @Autowired
    private CustomerProductRepository customerProductRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getAllCustomerProducts() {
        logger.info("获取所有客户产品关联列表");
        List<CustomerProduct> customerProducts = customerProductRepository.findAll();
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取客户产品关联列表，共{}条", customerProductDTOs.size());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<CustomerProductDTO> getCustomerProductById(@PathVariable Long id) {
        logger.info("获取客户产品关联详情，ID: {}", id);
        CustomerProduct customerProduct = customerProductRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户产品关联不存在，ID: {}", id);
                    return new RuntimeException("CustomerProduct not found");
                });
        logger.info("成功获取客户产品关联，ID: {}, 客户ID: {}, 产品ID: {}", 
                id, customerProduct.getCustomer().getId(), customerProduct.getProduct().getId());
        return ResponseEntity.ok(convertToDTO(customerProduct));
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getCustomerProductsByCustomerId(@PathVariable Long customerId) {
        logger.info("按客户ID查询产品关联列表，客户ID: {}", customerId);
        List<CustomerProduct> customerProducts = customerProductRepository.findByCustomerId(customerId);
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取客户[{}]的产品关联列表，共{}条", customerId, customerProductDTOs.size());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('customer-product:read')")
    public ResponseEntity<List<CustomerProductDTO>> getCustomerProductsByProductId(@PathVariable Long productId) {
        logger.info("按产品ID查询客户关联列表，产品ID: {}", productId);
        List<CustomerProduct> customerProducts = customerProductRepository.findByProductId(productId);
        List<CustomerProductDTO> customerProductDTOs = customerProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取产品[{}]的客户关联列表，共{}条", productId, customerProductDTOs.size());
        return ResponseEntity.ok(customerProductDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('customer-product:create')")
    public ResponseEntity<?> createCustomerProduct(@RequestBody CustomerProductDTO customerProductDTO) {
        logger.info("创建客户产品关联，客户ID: {}, 产品ID: {}", 
                customerProductDTO.getCustomerId(), customerProductDTO.getProductId());
        
        Customer customer = customerRepository.findById(customerProductDTO.getCustomerId())
                .orElseThrow(() -> {
                    logger.error("客户不存在，ID: {}", customerProductDTO.getCustomerId());
                    return new RuntimeException("Customer not found");
                });
        
        Product product = productRepository.findById(customerProductDTO.getProductId())
                .orElseThrow(() -> {
                    logger.error("产品不存在，ID: {}", customerProductDTO.getProductId());
                    return new RuntimeException("Product not found");
                });
        
        // 生成32位客户产品代码
        String customerProductCode = generateRandomCode(32);
        logger.debug("生成客户产品代码: {}", customerProductCode);
        
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
        logger.info("客户产品关联创建成功，ID: {}, 客户名称: {}, 产品名称: {}", 
                savedCustomerProduct.getId(), customer.getName(), product.getName());
        return ResponseEntity.ok(convertToDTO(savedCustomerProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:update')")
    public ResponseEntity<?> updateCustomerProduct(@PathVariable Long id, @RequestBody CustomerProductDTO customerProductDTO) {
        logger.info("更新客户产品关联，ID: {}", id);
        CustomerProduct customerProduct = customerProductRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("客户产品关联不存在，ID: {}", id);
                    return new RuntimeException("CustomerProduct not found");
                });
        
        logger.debug("更新前 - 客户ID: {}, 产品ID: {}, 客户价格: {}", 
                customerProduct.getCustomer().getId(), customerProduct.getProduct().getId(), customerProduct.getCustomerPrice());
        
        if (customerProductDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(customerProductDTO.getCustomerId())
                    .orElseThrow(() -> {
                        logger.error("客户不存在，ID: {}", customerProductDTO.getCustomerId());
                        return new RuntimeException("Customer not found");
                    });
            customerProduct.setCustomer(customer);
        }
        
        if (customerProductDTO.getProductId() != null) {
            Product product = productRepository.findById(customerProductDTO.getProductId())
                    .orElseThrow(() -> {
                        logger.error("产品不存在，ID: {}", customerProductDTO.getProductId());
                        return new RuntimeException("Product not found");
                    });
            customerProduct.setProduct(product);
        }
        
        customerProduct.setCustomerPrice(customerProductDTO.getCustomerPrice());
        customerProduct.setStockQuantity(customerProductDTO.getStockQuantity());
        customerProduct.setShipmentQuantity(customerProductDTO.getShipmentQuantity());
        customerProduct.setStockAmount(customerProductDTO.getStockAmount());
        customerProduct.setShipmentAmount(customerProductDTO.getShipmentAmount());
        customerProduct.setEnabled(customerProductDTO.getEnabled() != null ? customerProductDTO.getEnabled() : true);
        
        CustomerProduct updatedCustomerProduct = customerProductRepository.save(customerProduct);
        logger.info("客户产品关联更新成功，ID: {}, 客户名称: {}, 产品名称: {}", 
                updatedCustomerProduct.getId(), updatedCustomerProduct.getCustomer().getName(), updatedCustomerProduct.getProduct().getName());
        return ResponseEntity.ok(convertToDTO(updatedCustomerProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer-product:delete')")
    public ResponseEntity<?> deleteCustomerProduct(@PathVariable Long id) {
        logger.info("删除客户产品关联，ID: {}", id);
        customerProductRepository.deleteById(id);
        logger.info("客户产品关联删除成功，ID: {}", id);
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
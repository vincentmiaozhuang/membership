package com.membership.controller;

import com.membership.dto.ProductDTO;
import com.membership.core.entity.Product;
import com.membership.core.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        logger.info("获取所有产品列表");
        List<Product> products = productRepository.findAllByOrderByIdDesc();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取产品列表，共{}条", productDTOs.size());
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        logger.info("获取产品详情，ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("产品不存在，ID: {}", id);
                    return new RuntimeException("Product not found");
                });
        logger.info("成功获取产品，ID: {}, 名称: {}", id, product.getName());
        return ResponseEntity.ok(convertToDTO(product));
    }
    
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<List<ProductDTO>> getEnabledProducts() {
        logger.info("获取可用产品列表");
        List<Product> products = productRepository.findByEnabledTrue();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取可用产品列表，共{}条", productDTOs.size());
        return ResponseEntity.ok(productDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('product:create')")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        logger.info("创建产品，名称: {}", productDTO.getName());
        
        if (productRepository.existsByName(productDTO.getName())) {
            logger.warn("产品名称已存在，创建失败: {}", productDTO.getName());
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Product name is already taken!"));
        }
        
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setType(productDTO.getType());
        product.setFaceValue(productDTO.getFaceValue());
        product.setDescription(productDTO.getDescription());
        product.setEnabled(productDTO.getEnabled() != null ? productDTO.getEnabled() : true);
        
        Product savedProduct = productRepository.save(product);
        logger.info("产品创建成功，ID: {}, 名称: {}", savedProduct.getId(), savedProduct.getName());
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        logger.info("更新产品，ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("产品不存在，ID: {}", id);
                    return new RuntimeException("Product not found");
                });
        
        // Check if name is changed and if new name already exists
        if (!product.getName().equals(productDTO.getName()) && 
            productRepository.existsByName(productDTO.getName())) {
            logger.warn("产品名称已存在，更新失败: {}", productDTO.getName());
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Product name is already taken!"));
        }
        
        product.setName(productDTO.getName());
        product.setType(productDTO.getType());
        product.setFaceValue(productDTO.getFaceValue());
        product.setDescription(productDTO.getDescription());
        product.setEnabled(productDTO.getEnabled() != null ? productDTO.getEnabled() : true);
        
        Product updatedProduct = productRepository.save(product);
        logger.info("产品更新成功，ID: {}, 名称: {}", updatedProduct.getId(), updatedProduct.getName());
        return ResponseEntity.ok(convertToDTO(updatedProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:delete')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        logger.info("删除产品，ID: {}", id);
        productRepository.deleteById(id);
        logger.info("产品删除成功，ID: {}", id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Product deleted successfully"));
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setType(product.getType());
        dto.setFaceValue(product.getFaceValue());
        dto.setDescription(product.getDescription());
        dto.setEnabled(product.getEnabled());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
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

package com.membership.controller;

import com.membership.dto.ProductDTO;
import com.membership.entity.Product;
import com.membership.repository.ProductRepository;
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
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(convertToDTO(product));
    }
    
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<List<ProductDTO>> getEnabledProducts() {
        List<Product> products = productRepository.findByEnabledTrue();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('product:create')")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        if (productRepository.existsByName(productDTO.getName())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Product name is already taken!"));
        }
        
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setProductCode(generateProductCode());
        product.setType(productDTO.getType());
        product.setFaceValue(productDTO.getFaceValue());
        product.setDescription(productDTO.getDescription());
        product.setEnabled(productDTO.getEnabled() != null ? productDTO.getEnabled() : true);
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if name is changed and if new name already exists
        if (!product.getName().equals(productDTO.getName()) && 
            productRepository.existsByName(productDTO.getName())) {
            return ResponseEntity.badRequest()
                    .body(new com.membership.dto.MessageResponse("Error: Product name is already taken!"));
        }
        
        product.setName(productDTO.getName());
        product.setType(productDTO.getType());
        product.setFaceValue(productDTO.getFaceValue());
        product.setDescription(productDTO.getDescription());
        product.setEnabled(productDTO.getEnabled() != null ? productDTO.getEnabled() : true);
        
        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(updatedProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:delete')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok(new com.membership.dto.MessageResponse("Product deleted successfully"));
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setProductCode(product.getProductCode());
        dto.setType(product.getType());
        dto.setFaceValue(product.getFaceValue());
        dto.setDescription(product.getDescription());
        dto.setEnabled(product.getEnabled());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
    
    private String generateProductCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        // 检查是否已存在，避免重复
        while (productRepository.existsByProductCode(code.toString())) {
            code.setLength(0);
            for (int i = 0; i < 16; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        return code.toString();
    }
}

package com.membership.controller;

import com.membership.dto.SupplierProductDTO;
import com.membership.core.entity.Product;
import com.membership.core.entity.Supplier;
import com.membership.core.entity.SupplierProduct;
import com.membership.core.repository.ProductRepository;
import com.membership.core.repository.SupplierProductRepository;
import com.membership.core.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-products")
public class SupplierProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierProductController.class);
    
    @Autowired
    private SupplierProductRepository supplierProductRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    private String generateSupplierProductCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(16);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        // 检查是否已存在，避免重复
        while (supplierProductRepository.existsBySupplierProductCode(code.toString())) {
            code.setLength(0);
            for (int i = 0; i < 16; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        return code.toString();
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-product:read')")
    public ResponseEntity<?> getSupplierProducts() {
        logger.info("获取所有供应商产品关联记录");
        List<SupplierProduct> supplierProducts = supplierProductRepository.findAll();
        logger.debug("共查询到{}条供应商产品关联记录", supplierProducts.size());
        
        // 为缺失supplierProductCode的记录生成代码
        int codeGeneratedCount = 0;
        for (SupplierProduct product : supplierProducts) {
            if (product.getSupplierProductCode() == null || product.getSupplierProductCode().isEmpty()) {
                product.setSupplierProductCode(generateSupplierProductCode());
                supplierProductRepository.save(product);
                codeGeneratedCount++;
            }
        }
        if (codeGeneratedCount > 0) {
            logger.info("为{}条记录补充了供应商产品代码", codeGeneratedCount);
        }
        
        List<SupplierProductDTO> supplierProductDTOs = supplierProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取供应商产品关联记录，共{}条", supplierProductDTOs.size());
        return ResponseEntity.ok(supplierProductDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-product:create')")
    public ResponseEntity<?> createSupplierProduct(@RequestBody SupplierProductDTO supplierProductDTO) {
        logger.info("创建供应商产品关联记录，产品ID: {}, 供应商ID: {}", 
                supplierProductDTO.getProductId(), supplierProductDTO.getSupplierId());
        logger.debug("供应商价格: {}, 库存数量: {}, 面值: {}", 
                supplierProductDTO.getSupplierPrice(), supplierProductDTO.getStockQuantity(), supplierProductDTO.getFaceValue());
        
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.setProductId(supplierProductDTO.getProductId());
        supplierProduct.setSupplierId(supplierProductDTO.getSupplierId());
        supplierProduct.setSupplierPrice(supplierProductDTO.getSupplierPrice());
        supplierProduct.setStockQuantity(supplierProductDTO.getStockQuantity());
        supplierProduct.setSalesQuantity(supplierProductDTO.getSalesQuantity());
        supplierProduct.setStockAmount(supplierProductDTO.getStockAmount());
        supplierProduct.setSalesAmount(supplierProductDTO.getSalesAmount());
        supplierProduct.setEnabled(supplierProductDTO.getEnabled() != null ? supplierProductDTO.getEnabled() : true);
        supplierProduct.setSupplierProductCode(supplierProductDTO.getSupplierProductCode());
        supplierProduct.setFaceValue(supplierProductDTO.getFaceValue());
        supplierProduct.setDailyStockLimit(supplierProductDTO.getDailyStockLimit());
        
        SupplierProduct savedSupplierProduct = supplierProductRepository.save(supplierProduct);
        logger.info("供应商产品关联记录创建成功，ID: {}, 产品ID: {}, 供应商ID: {}", 
                savedSupplierProduct.getId(), savedSupplierProduct.getProductId(), savedSupplierProduct.getSupplierId());
        
        return ResponseEntity.ok(convertToDTO(savedSupplierProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-product:update')")
    public ResponseEntity<?> updateSupplierProduct(@PathVariable Long id, @RequestBody SupplierProductDTO supplierProductDTO) {
        logger.info("更新供应商产品关联记录，ID: {}", id);
        SupplierProduct supplierProduct = supplierProductRepository.findById(id).orElse(null);
        if (supplierProduct == null) {
            logger.warn("供应商产品关联记录不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        logger.debug("更新前 - 产品ID: {}, 供应商ID: {}, 供应商价格: {}", 
                supplierProduct.getProductId(), supplierProduct.getSupplierId(), supplierProduct.getSupplierPrice());
        
        supplierProduct.setProductId(supplierProductDTO.getProductId());
        supplierProduct.setSupplierId(supplierProductDTO.getSupplierId());
        supplierProduct.setSupplierPrice(supplierProductDTO.getSupplierPrice());
        supplierProduct.setStockQuantity(supplierProductDTO.getStockQuantity());
        supplierProduct.setSalesQuantity(supplierProductDTO.getSalesQuantity());
        supplierProduct.setStockAmount(supplierProductDTO.getStockAmount());
        supplierProduct.setSalesAmount(supplierProductDTO.getSalesAmount());
        supplierProduct.setEnabled(supplierProductDTO.getEnabled() != null ? supplierProductDTO.getEnabled() : true);
        supplierProduct.setSupplierProductCode(supplierProductDTO.getSupplierProductCode());
        supplierProduct.setFaceValue(supplierProductDTO.getFaceValue());
        supplierProduct.setDailyStockLimit(supplierProductDTO.getDailyStockLimit());
        
        SupplierProduct updatedSupplierProduct = supplierProductRepository.save(supplierProduct);
        logger.info("供应商产品关联记录更新成功，ID: {}, 产品ID: {}, 供应商ID: {}", 
                updatedSupplierProduct.getId(), updatedSupplierProduct.getProductId(), updatedSupplierProduct.getSupplierId());
        
        return ResponseEntity.ok(convertToDTO(updatedSupplierProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-product:delete')")
    public ResponseEntity<?> deleteSupplierProduct(@PathVariable Long id) {
        logger.info("删除供应商产品关联记录，ID: {}", id);
        supplierProductRepository.deleteById(id);
        logger.info("供应商产品关联记录删除成功，ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('supplier-product:read')")
    public ResponseEntity<?> getSupplierProductsByProductId(@PathVariable Long productId) {
        logger.info("获取产品[{}]的供应商关联记录", productId);
        List<SupplierProduct> supplierProducts = supplierProductRepository.findByProductId(productId);
        List<SupplierProductDTO> supplierProductDTOs = supplierProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取产品[{}]的供应商关联记录，共{}条", productId, supplierProductDTOs.size());
        return ResponseEntity.ok(supplierProductDTOs);
    }
    
    private SupplierProductDTO convertToDTO(SupplierProduct supplierProduct) {
        SupplierProductDTO dto = new SupplierProductDTO();
        dto.setId(supplierProduct.getId());
        dto.setProductId(supplierProduct.getProductId());
        dto.setSupplierId(supplierProduct.getSupplierId());
        dto.setSupplierPrice(supplierProduct.getSupplierPrice());
        dto.setStockQuantity(supplierProduct.getStockQuantity());
        dto.setSalesQuantity(supplierProduct.getSalesQuantity());
        dto.setStockAmount(supplierProduct.getStockAmount());
        dto.setSalesAmount(supplierProduct.getSalesAmount());
        dto.setEnabled(supplierProduct.getEnabled());
        dto.setCreatedAt(supplierProduct.getCreatedAt());
        dto.setUpdatedAt(supplierProduct.getUpdatedAt());
        dto.setSupplierProductCode(supplierProduct.getSupplierProductCode());
        dto.setFaceValue(supplierProduct.getFaceValue());
        dto.setDailyStockLimit(supplierProduct.getDailyStockLimit());
        
        // 关联产品信息
        Product product = productRepository.findById(supplierProduct.getProductId()).orElse(null);
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductType(product.getType());
            dto.setProductFaceValue(product.getFaceValue());
        }
        
        // 关联供应商信息
        Supplier supplier = supplierRepository.findById(supplierProduct.getSupplierId()).orElse(null);
        if (supplier != null) {
            dto.setSupplierName(supplier.getName());
            dto.setSupplierCode(supplier.getSupplierCode());
        }
        
        return dto;
    }
}
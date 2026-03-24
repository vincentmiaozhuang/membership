package com.membership.controller;

import com.membership.dto.SupplierProductDTO;
import com.membership.entity.Product;
import com.membership.entity.Supplier;
import com.membership.entity.SupplierProduct;
import com.membership.repository.ProductRepository;
import com.membership.repository.SupplierProductRepository;
import com.membership.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/supplier-products")
public class SupplierProductController {
    
    @Autowired
    private SupplierProductRepository supplierProductRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @GetMapping
    @PreAuthorize("hasAuthority('supplier-product:read')")
    public ResponseEntity<?> getSupplierProducts() {
        List<SupplierProduct> supplierProducts = supplierProductRepository.findAll();
        List<SupplierProductDTO> supplierProductDTOs = supplierProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplierProductDTOs);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('supplier-product:create')")
    public ResponseEntity<?> createSupplierProduct(@RequestBody SupplierProductDTO supplierProductDTO) {
        SupplierProduct supplierProduct = new SupplierProduct();
        supplierProduct.setProductId(supplierProductDTO.getProductId());
        supplierProduct.setSupplierId(supplierProductDTO.getSupplierId());
        supplierProduct.setSupplierPrice(supplierProductDTO.getSupplierPrice());
        supplierProduct.setStockQuantity(supplierProductDTO.getStockQuantity());
        supplierProduct.setSalesQuantity(supplierProductDTO.getSalesQuantity());
        supplierProduct.setStockAmount(supplierProductDTO.getStockAmount());
        supplierProduct.setSalesAmount(supplierProductDTO.getSalesAmount());
        supplierProduct.setEnabled(supplierProductDTO.getEnabled() != null ? supplierProductDTO.getEnabled() : true);
        
        SupplierProduct savedSupplierProduct = supplierProductRepository.save(supplierProduct);
        return ResponseEntity.ok(convertToDTO(savedSupplierProduct));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-product:update')")
    public ResponseEntity<?> updateSupplierProduct(@PathVariable Long id, @RequestBody SupplierProductDTO supplierProductDTO) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(id).orElse(null);
        if (supplierProduct == null) {
            return ResponseEntity.notFound().build();
        }
        
        supplierProduct.setProductId(supplierProductDTO.getProductId());
        supplierProduct.setSupplierId(supplierProductDTO.getSupplierId());
        supplierProduct.setSupplierPrice(supplierProductDTO.getSupplierPrice());
        supplierProduct.setStockQuantity(supplierProductDTO.getStockQuantity());
        supplierProduct.setSalesQuantity(supplierProductDTO.getSalesQuantity());
        supplierProduct.setStockAmount(supplierProductDTO.getStockAmount());
        supplierProduct.setSalesAmount(supplierProductDTO.getSalesAmount());
        supplierProduct.setEnabled(supplierProductDTO.getEnabled() != null ? supplierProductDTO.getEnabled() : true);
        
        SupplierProduct updatedSupplierProduct = supplierProductRepository.save(supplierProduct);
        return ResponseEntity.ok(convertToDTO(updatedSupplierProduct));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier-product:delete')")
    public ResponseEntity<?> deleteSupplierProduct(@PathVariable Long id) {
        supplierProductRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('supplier-product:read')")
    public ResponseEntity<?> getSupplierProductsByProductId(@PathVariable Long productId) {
        List<SupplierProduct> supplierProducts = supplierProductRepository.findByProductId(productId);
        List<SupplierProductDTO> supplierProductDTOs = supplierProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        
        // 关联产品信息
        Product product = productRepository.findById(supplierProduct.getProductId()).orElse(null);
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductCode(product.getProductCode());
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
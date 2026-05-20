package com.membership.core.repository;

import com.membership.core.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {
    List<SupplierProduct> findByProductId(Long productId);
    boolean existsBySupplierProductCode(String supplierProductCode);
}

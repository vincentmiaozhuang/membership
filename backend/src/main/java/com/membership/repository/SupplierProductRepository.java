package com.membership.repository;

import com.membership.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {
    List<SupplierProduct> findByProductId(Long productId);
}

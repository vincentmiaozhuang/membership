package com.membership.repository;

import com.membership.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByEnabledTrue();
    
    List<Supplier> findByNameContaining(String name);
    
    boolean existsBySupplierCode(String supplierCode);
}

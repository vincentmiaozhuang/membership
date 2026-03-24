package com.membership.repository;

import com.membership.entity.SupplierBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierBalanceRepository extends JpaRepository<SupplierBalance, Long> {
    
    Optional<SupplierBalance> findBySupplierId(Long supplierId);
}

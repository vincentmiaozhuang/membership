package com.membership.repository;

import com.membership.entity.SupplierRecharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRechargeRepository extends JpaRepository<SupplierRecharge, Long> {
    List<SupplierRecharge> findBySupplierId(Long supplierId);
}

package com.membership.repository;

import com.membership.entity.CustomerProductSupplierOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerProductSupplierOrderRepository extends JpaRepository<CustomerProductSupplierOrder, Long> {
    List<CustomerProductSupplierOrder> findByCustomerProductId(Long customerProductId);
    void deleteByCustomerProductId(Long customerProductId);
}

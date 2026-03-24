package com.membership.repository;

import com.membership.entity.CustomerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerProductRepository extends JpaRepository<CustomerProduct, Long> {
    List<CustomerProduct> findByCustomerId(Long customerId);
    
    List<CustomerProduct> findByProductId(Long productId);
    
    boolean existsByProductId(Long productId);
}

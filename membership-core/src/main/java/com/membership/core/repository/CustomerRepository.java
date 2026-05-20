package com.membership.core.repository;

import com.membership.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    
    List<Customer> findByEnabledTrue();
    
    List<Customer> findByNameContaining(String name);
    
    boolean existsByCustomerCode(String customerCode);
}
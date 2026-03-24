package com.membership.repository;

import com.membership.entity.CustomerBalance;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerBalanceRepository extends org.springframework.data.jpa.repository.JpaRepository<CustomerBalance, Long> {
    
    Optional<CustomerBalance> findByCustomerId(Long customerId);
    
    void deleteByCustomerId(Long customerId);
}

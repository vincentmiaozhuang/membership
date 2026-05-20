package com.membership.core.repository;

import com.membership.core.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    Message findByIdAndUserId(Long id, Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.userId = :userId")
    void markAllAsRead(Long userId);
}
package com.membership.controller;

import com.membership.dto.MessageDTO;
import com.membership.dto.MessageResponse;
import com.membership.entity.Message;
import com.membership.repository.MessageRepository;
import com.membership.security.UserDetailsImpl;
import com.membership.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private MessageService messageService;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        logger.info("获取用户[{}]的未读消息数量", userId);
        long count = messageRepository.countByUserIdAndIsReadFalse(userId);
        logger.info("用户[{}]的未读消息数量为: {}", userId, count);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages() {
        Long userId = getCurrentUserId();
        logger.info("获取用户[{}]的消息列表", userId);
        List<Message> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("成功获取用户[{}]的消息列表，共{}条", userId, messageDTOs.size());
        return ResponseEntity.ok(messageDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("获取用户[{}]的消息详情，消息ID: {}", userId, id);
        Message message = messageRepository.findByIdAndUserId(id, userId);
        if (message == null) {
            logger.warn("用户[{}]查找消息[{}]失败：消息不存在或无权限", userId, id);
            return ResponseEntity.notFound().build();
        }
        
        if (!message.getIsRead()) {
            message.setIsRead(true);
            messageRepository.save(message);
            logger.info("消息[{}]已标记为已读", id);
        }
        
        logger.info("成功获取消息[{}]详情，标题: {}", id, message.getTitle());
        return ResponseEntity.ok(convertToDTO(message));
    }
    
    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(@RequestBody MessageDTO messageDTO) {
        Long userId = getCurrentUserId();
        logger.info("用户[{}]创建站内信，标题: {}", userId, messageDTO.getTitle());
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(messageDTO.getTitle());
        message.setContent(messageDTO.getContent());
        message.setIsRead(false);
        
        Message savedMessage = messageRepository.save(message);
        logger.info("站内信创建成功，消息ID: {}, 用户ID: {}, 标题: {}", 
                savedMessage.getId(), userId, savedMessage.getTitle());
        return ResponseEntity.ok(convertToDTO(savedMessage));
    }
    
    /**
     * 管理员向指定用户发送站内信
     * @param userId 接收消息的用户ID
     * @param messageDTO 消息内容（包含title和content）
     */
    @PostMapping("/send-to/{userId}")
    public ResponseEntity<MessageDTO> sendMessageToUser(
            @PathVariable Long userId, 
            @RequestBody MessageDTO messageDTO) {
        Long currentUserId = getCurrentUserId();
        logger.info("管理员[{}]向用户[{}]发送站内信，标题: {}", currentUserId, userId, messageDTO.getTitle());
        MessageDTO savedMessage = messageService.sendMessage(userId, messageDTO.getTitle(), messageDTO.getContent());
        logger.info("站内信发送成功，消息ID: {}, 接收用户ID: {}, 标题: {}", 
                savedMessage.getId(), userId, savedMessage.getTitle());
        return ResponseEntity.ok(savedMessage);
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("用户[{}]标记消息[{}]为已读", userId, id);
        Message message = messageRepository.findByIdAndUserId(id, userId);
        if (message == null) {
            logger.warn("用户[{}]标记消息[{}]为已读失败：消息不存在或无权限", userId, id);
            return ResponseEntity.notFound().build();
        }
        
        message.setIsRead(true);
        Message updatedMessage = messageRepository.save(message);
        logger.info("消息[{}]已成功标记为已读", id);
        return ResponseEntity.ok(convertToDTO(updatedMessage));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("用户[{}]删除消息[{}]", userId, id);
        Message message = messageRepository.findByIdAndUserId(id, userId);
        if (message == null) {
            logger.warn("用户[{}]删除消息[{}]失败：消息不存在或无权限", userId, id);
            return ResponseEntity.notFound().build();
        }
        
        messageRepository.deleteById(id);
        logger.info("消息[{}]已成功删除", id);
        return ResponseEntity.ok(new MessageResponse("Message deleted successfully"));
    }
    
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setUserId(message.getUserId());
        dto.setTitle(message.getTitle());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }
}
package com.membership.core.service;

import com.membership.core.entity.Message;
import com.membership.core.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message sendMessage(Long userId, String title, String content) {
        logger.info("发送站内信 - 用户ID: {}, 标题: {}", userId, title);
        
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        logger.info("站内信发送成功 - 消息ID: {}", savedMessage.getId());
        
        return savedMessage;
    }

    @Transactional
    public int sendMessageToUsers(List<Long> userIds, String title, String content) {
        logger.info("批量发送站内信 - 用户数量: {}, 标题: {}", userIds.size(), title);
        
        int count = 0;
        for (Long userId : userIds) {
            try {
                sendMessage(userId, title, content);
                count++;
            } catch (Exception e) {
                logger.error("发送站内信失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            }
        }
        
        logger.info("批量发送完成 - 成功: {}, 失败: {}", count, userIds.size() - count);
        return count;
    }

    public List<Message> getUserMessages(Long userId, int page, int size) {
        logger.debug("获取用户消息列表 - 用户ID: {}, 页码: {}, 大小: {}", userId, page, size);
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public boolean markAsRead(Long messageId, Long userId) {
        logger.info("标记消息为已读 - 消息ID: {}, 用户ID: {}", messageId, userId);
        
        Message message = messageRepository.findByIdAndUserId(messageId, userId);
        if (message != null) {
            message.setIsRead(true);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
            logger.info("消息已标记为已读 - 消息ID: {}", messageId);
            return true;
        }
        return false;
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        logger.info("标记用户所有消息为已读 - 用户ID: {}", userId);
        messageRepository.markAllAsRead(userId);
    }

    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        logger.info("删除消息 - 消息ID: {}, 用户ID: {}", messageId, userId);
        
        Message message = messageRepository.findByIdAndUserId(messageId, userId);
        if (message != null) {
            messageRepository.delete(message);
            logger.info("消息删除成功 - 消息ID: {}", messageId);
            return true;
        }
        return false;
    }
}
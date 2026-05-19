package com.membership.service;

import com.membership.dto.MessageDTO;
import com.membership.entity.Message;
import com.membership.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 站内信服务类
 * 提供生成和发送站内信的工具方法
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 向指定用户发送站内信
     *
     * @param userId  接收消息的用户ID
     * @param title   消息标题
     * @param content 消息内容
     * @return 创建的消息DTO
     */
    public MessageDTO sendMessage(Long userId, String title, String content) {
        logger.info("准备发送站内信，用户ID: {}, 标题: {}, 内容长度: {}", userId, title, content.length());
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);
        logger.info("站内信发送成功，消息ID: {}, 用户ID: {}", savedMessage.getId(), userId);
        return convertToDTO(savedMessage);
    }

    /**
     * 向指定用户发送站内信（重载方法，使用Message实体）
     *
     * @param message 消息实体
     * @return 创建的消息DTO
     */
    public MessageDTO sendMessage(Message message) {
        logger.info("准备发送站内信（实体方式），用户ID: {}, 标题: {}", 
                message.getUserId(), message.getTitle());
        message.setIsRead(false);
        Message savedMessage = messageRepository.save(message);
        logger.info("站内信发送成功，消息ID: {}, 用户ID: {}", savedMessage.getId(), message.getUserId());
        return convertToDTO(savedMessage);
    }

    /**
     * 批量发送站内信给多个用户
     *
     * @param userIds 接收消息的用户ID列表
     * @param title   消息标题
     * @param content 消息内容
     */
    public void sendMessageToUsers(java.util.List<Long> userIds, String title, String content) {
        logger.info("准备批量发送站内信，目标用户数: {}, 标题: {}", userIds.size(), title);
        int successCount = 0;
        int failCount = 0;
        for (Long userId : userIds) {
            try {
                sendMessage(userId, title, content);
                successCount++;
            } catch (Exception e) {
                failCount++;
                logger.error("向用户[{}]发送站内信失败: {}", userId, e.getMessage());
            }
        }
        logger.info("批量发送完成，成功: {}, 失败: {}", successCount, failCount);
    }

    /**
     * 将Message实体转换为MessageDTO
     */
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

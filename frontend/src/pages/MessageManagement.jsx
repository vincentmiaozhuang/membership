import React, { useState, useEffect } from 'react'
import { useHistory } from 'react-router-dom'
import { List, Avatar, Badge, Empty, Spin, message } from 'antd'
import { MailOutlined, ClockCircleOutlined } from '@ant-design/icons'
import request from '../utils/request'

const MessageManagement = () => {
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const history = useHistory()

  useEffect(() => {
    fetchMessages()
  }, [])

  const fetchMessages = async () => {
    setLoading(true)
    try {
      const data = await request.get('/messages')
      setMessages(data)
    } catch (error) {
      message.error('获取消息失败')
    } finally {
      setLoading(false)
    }
  }

  const handleMessageClick = (message) => {
    history.push(`/messages/${message.id}`)
  }

  const formatDateTime = (dateTime) => {
    if (!dateTime) return ''
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getContentPreview = (content) => {
    if (!content) return ''
    if (content.length <= 50) {
      return content
    }
    return content.substring(0, 50) + '...'
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '200px' }}>
        <Spin />
      </div>
    )
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <h2>站内信</h2>
        <span style={{ color: '#999', fontSize: 14 }}>共 {messages.length} 条消息</span>
      </div>
      
      {messages.length === 0 ? (
        <Empty description="暂无消息" />
      ) : (
        <div className="message-list">
          {messages.map((message) => (
            <div
              key={message.id}
              onClick={() => handleMessageClick(message)}
              style={{ 
                cursor: 'pointer', 
                padding: '16px 20px', 
                border: '1px solid #e8e8e8',
                borderRadius: '4px',
                marginBottom: 12,
                transition: 'all 0.2s',
                backgroundColor: message.isRead ? '#fff' : '#fff7f7'
              }}
              onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.08)'}
              onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: 16 }}>
                <Badge 
                  dot={!message.isRead} 
                  color="#ff4d4f"
                  style={{ top: -2, right: -2 }}
                >
                  <Avatar icon={<MailOutlined />} style={{ backgroundColor: '#1890ff' }} />
                </Badge>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                    <span style={{ 
                      fontWeight: message.isRead ? 'normal' : 'bold',
                      fontSize: 15,
                      color: '#333'
                    }}>
                      {message.title}
                    </span>
                    {!message.isRead && (
                      <span style={{ 
                        width: 8, 
                        height: 8, 
                        borderRadius: '50%', 
                        backgroundColor: '#ff4d4f' 
                      }} />
                    )}
                  </div>
                  <p style={{ 
                    margin: 0, 
                    color: '#666', 
                    fontSize: 14, 
                    lineHeight: '1.5',
                    marginBottom: 8
                  }}>
                    {getContentPreview(message.content)}
                  </p>
                  <p style={{ 
                    margin: 0, 
                    color: '#999', 
                    fontSize: 12, 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: 4 
                  }}>
                    <ClockCircleOutlined size={12} />
                    {formatDateTime(message.createdAt)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default MessageManagement
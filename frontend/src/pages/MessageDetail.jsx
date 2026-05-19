import React, { useState, useEffect } from 'react'
import { useParams, useHistory } from 'react-router-dom'
import { Button, Card, Tag, Empty, Spin } from 'antd'
import { ArrowLeftOutlined, ClockCircleOutlined } from '@ant-design/icons'
import request from '../utils/request'

const MessageDetail = () => {
  const { id } = useParams()
  const history = useHistory()
  const [message, setMessage] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchMessage()
  }, [id])

  const fetchMessage = async () => {
    try {
      setLoading(true)
      const data = await request.get(`/messages/${id}`)
      setMessage(data)
    } catch (error) {
      console.error('获取消息详情失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatDateTime = (dateTime) => {
    if (!dateTime) return ''
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }

  const handleBack = () => {
    history.goBack()
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '200px' }}>
        <Spin />
      </div>
    )
  }

  if (!message) {
    return <Empty description="消息不存在" />
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={handleBack} icon={<ArrowLeftOutlined />}>
          返回
        </Button>
        <h2>消息详情</h2>
      </div>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 'bold' }}>
              {message.title}
            </h3>
            <Tag color={message.isRead ? 'green' : 'red'}>
              {message.isRead ? '已读' : '未读'}
            </Tag>
          </div>
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

        <div style={{ 
          padding: 16, 
          backgroundColor: '#fafafa', 
          borderRadius: '4px',
          minHeight: '200px'
        }}>
          <p style={{ margin: 0, lineHeight: '1.8', fontSize: 14, color: '#333' }}>
            {message.content}
          </p>
        </div>
      </Card>
    </div>
  )
}

export default MessageDetail
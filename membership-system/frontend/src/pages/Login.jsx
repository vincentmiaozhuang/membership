import React, { useState } from 'react'
import { useHistory } from 'react-router-dom'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useAuth } from '../contexts/AuthContext'

const Login = () => {
  const history = useHistory()
  const { login } = useAuth()
  const [loading, setLoading] = useState(false)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  const handleLogin = async () => {
    console.log('登录请求参数:', { username, password })
    if (!username || !password) {
      message.error('请输入用户名和密码')
      return
    }
    
    setLoading(true)
    try {
      console.log('发起登录请求...')
      await login(username, password)
      message.success('登录成功')
      history.push('/')
    } catch (error) {
      console.error('登录失败，错误信息:', error)
      // 显示后端返回的错误信息
      const errorMessage = error.response?.data?.message || '登录失败，请检查用户名和密码'
      message.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <Card className="login-box">
        <h2 className="login-title">会员卡管理系统</h2>
        <div style={{ marginBottom: '24px' }}>
          <div style={{ marginBottom: '16px' }}>
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              style={{ height: '48px', fontSize: '16px' }}
            />
          </div>
          <div style={{ marginBottom: '24px' }}>
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={{ height: '48px', fontSize: '16px' }}
            />
          </div>
          <Button
            type="primary"
            onClick={handleLogin}
            loading={loading}
            block
            style={{ height: '48px', fontSize: '16px', fontWeight: '500' }}
          >
            登录
          </Button>
        </div>
        <div style={{ textAlign: 'center', color: '#999', fontSize: 12 }}>
          默认管理员账号：admin / admin123
        </div>
      </Card>
    </div>
  )
}

export default Login

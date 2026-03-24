import React, { createContext, useContext, useState, useEffect } from 'react'
import request from '../utils/request'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    if (token && savedUser) {
      setUser(JSON.parse(savedUser))
    }
    setLoading(false)
  }, [])

  const login = async (username, password) => {
    console.log('发起登录请求:', { username, password })
    try {
      const response = await request.post('/auth/login', { username, password })
      console.log('登录请求响应:', response)
      const { token, ...userInfo } = response
      console.log('提取的用户信息:', { token, userInfo })
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(userInfo))
      setUser(userInfo)
      return userInfo
    } catch (error) {
      console.error('登录请求失败:', error)
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  const hasPermission = (permission) => {
    if (!user || !user.permissions) return false
    return user.permissions.includes(permission)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, hasPermission, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

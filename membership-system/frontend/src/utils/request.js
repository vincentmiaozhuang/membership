import axios from 'axios'
import { message } from 'antd'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use(
  (config) => {
    console.log('发起请求:', config.url, config.data)
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    console.log('响应成功:', response.config.url, response.data)
    return response.data
  },
  (error) => {
    console.error('响应错误:', error)
    if (error.response) {
      const { status, data } = error.response
      console.error('响应错误详情:', { status, data })
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        message.error('登录已过期，请重新登录')
      } else if (status === 403) {
        message.error('没有权限执行此操作')
      } else if (status !== 400) {
        // 400错误由调用者处理，其他错误显示默认提示
        message.error(data?.message || '请求失败')
      }
    } else {
      message.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

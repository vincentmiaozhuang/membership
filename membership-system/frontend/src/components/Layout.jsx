import React from 'react'

import { useHistory, useLocation } from 'react-router-dom'
import { Layout, Menu, Button, Dropdown, Avatar } from 'antd'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  ShopOutlined,
  CreditCardOutlined,
  CustomerServiceOutlined,
  LogoutOutlined,
  DownOutlined,
  TransactionOutlined,
  WalletOutlined,
  SettingOutlined,
  BarChartOutlined,
} from '@ant-design/icons'
import { useAuth } from '../contexts/AuthContext'

const { Header, Sider, Content } = Layout

const CustomLayout = ({ children }) => {
  const history = useHistory()
  const location = useLocation()
  const { user, logout, hasPermission } = useAuth()

  // 定义所有菜单项及其对应的权限
  const allMenuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '首页',
      permission: null, // 首页不需要权限
    },
    {
      key: 'system',
      icon: <SettingOutlined />,
      label: '系统管理',
      permission: null,
      children: [
        {
          key: '/users',
          icon: <UserOutlined />,
          label: '用户管理',
          permission: 'user:read',
        },
        {
          key: '/roles',
          icon: <TeamOutlined />,
          label: '角色管理',
          permission: 'role:read',
        },
        {
          key: '/permissions',
          icon: <SafetyOutlined />,
          label: '权限管理',
          permission: 'permission:read',
        },
      ],
    },
    {
      key: 'product',
      icon: <CreditCardOutlined />,
      label: '产品',
      permission: null,
      children: [
        {
          key: '/products',
          icon: <CreditCardOutlined />,
          label: '产品管理',
          permission: 'product:read',
        },
      ],
    },
    {
      key: 'supplier',
      icon: <ShopOutlined />,
      label: '供应商',
      permission: null,
      children: [
        {
          key: '/suppliers',
          icon: <ShopOutlined />,
          label: '供应商管理',
          permission: 'supplier:read',
        },
        // {
        //   key: '/supplier-products',
        //   icon: <TransactionOutlined />,
        //   label: '供应商产品管理',
        //   permission: 'supplier-product:read',
        // },
        // {
        //   key: '/supplier-balances',
        //   icon: <WalletOutlined />,
        //   label: '供应商余额管理',
        //   permission: 'supplier-balance:read',
        // },
      ],
    },
    {
      key: 'customer',
      icon: <CustomerServiceOutlined />,
      label: '客户',
      permission: null,
      children: [
        {
          key: '/customers',
          icon: <CustomerServiceOutlined />,
          label: '客户管理',
          permission: 'customer:read',
        },
        // {
        //   key: '/customer-balances',
        //   icon: <WalletOutlined />,
        //   label: '客户余额管理',
        //   permission: 'customer-balance:read',
        // },
        // {
        //   key: '/customer-products',
        //   icon: <TransactionOutlined />,
        //   label: '客户产品管理',
        //   permission: 'customer-product:read',
        // },
        // {
        //   key: '/customer-payments',
        //   icon: <TransactionOutlined />,
        //   label: '客户付款管理',
        //   permission: 'customer-payment:read',
        // },
      ],
    },
    {
      key: 'recharge',
      icon: <TransactionOutlined />,
      label: '充值记录',
      permission: null,
      children: [
        {
          key: '/recharge-records',
          icon: <TransactionOutlined />,
          label: '充值记录管理',
          permission: 'recharge:read',
        },
      ],
    },
    {
      key: 'stats',
      icon: <BarChartOutlined />,
      label: '数据统计',
      permission: null,
      children: [
        {
          key: '/stats',
          icon: <BarChartOutlined />,
          label: '统计数据',
          permission: 'stats:read',
        },
        {
          key: '/daily-stats',
          icon: <BarChartOutlined />,
          label: '每日充值汇总',
          permission: 'daily-stats:read',
        },
      ],
    },
    
  ]

  // 根据权限过滤菜单项
  const menuItems = allMenuItems.filter(item => {
    // 首页始终显示
    if (!item.permission && !item.children) return true
    
    // 处理系统管理等带有子菜单的菜单项
    if (item.children) {
      // 过滤子菜单，只保留用户有权限的子菜单项
      const filteredChildren = item.children.filter(child => {
        // 子菜单如果没有权限设置，则始终显示
        if (!child.permission) return true
        // 检查用户是否有该子菜单的权限
        return hasPermission(child.permission)
      })
      
      // 如果过滤后还有子菜单项，则显示该菜单项
      if (filteredChildren.length > 0) {
        // 更新菜单项的子菜单为过滤后的子菜单
        item.children = filteredChildren
        return true
      } else {
        // 如果过滤后没有子菜单项，则不显示该菜单项
        return false
      }
    }
    
    // 检查用户是否有该权限
    return hasPermission(item.permission)
  })

  const handleMenuClick = ({ key }) => {
    history.push(key)
  }

  const handleLogout = () => {
    logout()
    history.push('/login')
  }

  const userMenu = (
    <Menu>
      <Menu.Item key="logout" onClick={handleLogout}>
        <LogoutOutlined /> 退出登录
      </Menu.Item>
    </Menu>
  )

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="light" width={200}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', borderBottom: '1px solid #f0f0f0' }}>
          <h3 style={{ margin: 0 }}>会员卡管理系统</h3>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          onClick={handleMenuClick}
          style={{ borderRight: 0 }}
        >
          {menuItems.map(item => {
            if (item.children) {
              return (
                <Menu.SubMenu key={item.key} icon={item.icon} title={item.label}>
                  {item.children.map(child => (
                    <Menu.Item key={child.key} icon={child.icon}>
                      {child.label}
                    </Menu.Item>
                  ))}
                </Menu.SubMenu>
              )
            } else {
              return (
                <Menu.Item key={item.key} icon={item.icon}>
                  {item.label}
                </Menu.Item>
              )
            }
          })}
        </Menu>
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)' }}>
          <div></div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span>欢迎，{user?.username}</span>
            <Dropdown overlay={userMenu} placement="bottomRight">
              <Button type="text">
                <Avatar icon={<UserOutlined />} size="small" />
                <DownOutlined style={{ marginLeft: 8 }} />
              </Button>
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: '24px', padding: '24px', background: '#fff', minHeight: '280px', borderRadius: '4px' }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}

export default CustomLayout

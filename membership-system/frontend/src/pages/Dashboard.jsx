import React from 'react'
import { Card, Row, Col, Statistic } from 'antd'
import {
  UserOutlined,
  TeamOutlined,
  ShopOutlined,
  CreditCardOutlined,
  CustomerServiceOutlined,
} from '@ant-design/icons'

const Dashboard = () => {
  return (
    <div>
      <h2>欢迎使用会员卡管理系统</h2>
      <Row gutter={16} style={{ marginTop: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="用户数量"
              value={0}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="角色数量"
              value={0}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="供应商数量"
              value={0}
              prefix={<ShopOutlined />}
            />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="产品数量"
              value={0}
              prefix={<CreditCardOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="客户数量"
              value={0}
              prefix={<CustomerServiceOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard

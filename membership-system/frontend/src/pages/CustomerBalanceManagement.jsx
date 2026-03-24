import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm, InputNumber, Switch, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const CustomerBalanceManagement = () => {
  const [balances, setBalances] = useState([])
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingBalance, setEditingBalance] = useState(null)
  const [form] = Form.useForm()

  const fetchBalances = async () => {
    setLoading(true)
    try {
      const data = await request.get('/customer-balances')
      setBalances(data)
    } catch (error) {
      message.error('获取客户余额列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchCustomers = async () => {
    try {
      const data = await request.get('/customers/enabled')
      setCustomers(data)
    } catch (error) {
      message.error('获取客户列表失败')
    }
  }

  useEffect(() => {
    fetchBalances()
    fetchCustomers()
  }, [])

  const handleAdd = () => {
    setEditingBalance(null)
    form.resetFields()
    form.setFieldsValue({
      alertThreshold: 0,
      enabled: true,
    })
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingBalance(record)
    form.setFieldsValue({
      customerId: record.customerId,
      paymentAccountTotal: record.paymentAccountTotal,
      paymentAccountConsumed: record.paymentAccountConsumed,
      paymentAccountBalance: record.paymentAccountBalance,
      creditAccountTotal: record.creditAccountTotal,
      creditAccountConsumed: record.creditAccountConsumed,
      creditAccountBalance: record.creditAccountBalance,
      totalAccountAmount: record.totalAccountAmount,
      totalAccountConsumed: record.totalAccountConsumed,
      totalAccountBalance: record.totalAccountBalance,
      alertThreshold: record.alertThreshold || 0,
      enabled: record.enabled !== undefined ? record.enabled : true,
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/customer-balances/${id}`)
      message.success('删除成功')
      fetchBalances()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      if (editingBalance) {
        await request.put(`/customer-balances/${editingBalance.id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/customer-balances', values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchBalances()
    } catch (error) {
      console.error(error)
    }
  }

  const columns = [
    {
      title: '客户名称',
      dataIndex: 'customerName',
      key: 'customerName',
    },
    {
      title: '付款账户总额',
      dataIndex: 'paymentAccountTotal',
      key: 'paymentAccountTotal',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '付款账户已消费金额',
      dataIndex: 'paymentAccountConsumed',
      key: 'paymentAccountConsumed',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '付款账户余额',
      dataIndex: 'paymentAccountBalance',
      key: 'paymentAccountBalance',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '授信账户总额',
      dataIndex: 'creditAccountTotal',
      key: 'creditAccountTotal',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '授信账户已消费金额',
      dataIndex: 'creditAccountConsumed',
      key: 'creditAccountConsumed',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '授信账户余额',
      dataIndex: 'creditAccountBalance',
      key: 'creditAccountBalance',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '账户总额',
      dataIndex: 'totalAccountAmount',
      key: 'totalAccountAmount',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '账户已消费总金额',
      dataIndex: 'totalAccountConsumed',
      key: 'totalAccountConsumed',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '账户总余额',
      dataIndex: 'totalAccountBalance',
      key: 'totalAccountBalance',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '差值提醒',
      dataIndex: 'alertThreshold',
      key: 'alertThreshold',
      render: (value) => value ? `¥${value.toFixed(2)}` : '¥0.00',
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? '正常使用' : '已停用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <span>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此客户余额记录吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </span>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增客户余额
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={balances}
        rowKey="id"
        loading={loading}
        scroll={{ x: 'max-content' }}
      />
      <Modal
        title={editingBalance ? '编辑客户余额' : '新增客户余额'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={800}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="customerId"
            label="客户"
            rules={[{ required: true, message: '请选择客户' }]}
          >
            <Select
              placeholder="请选择客户"
              disabled={editingBalance}
            >
              {customers.map((customer) => (
                <Select.Option key={customer.id} value={customer.id}>
                  {customer.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="paymentAccountTotal"
            label="付款账户总额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入付款账户总额" />
          </Form.Item>
          <Form.Item
            name="paymentAccountConsumed"
            label="付款账户已消费金额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入付款账户已消费金额" />
          </Form.Item>
          <Form.Item
            name="paymentAccountBalance"
            label="付款账户余额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入付款账户余额" />
          </Form.Item>
          <Form.Item
            name="creditAccountTotal"
            label="授信账户总额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入授信账户总额" />
          </Form.Item>
          <Form.Item
            name="creditAccountConsumed"
            label="授信账户已消费金额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入授信账户已消费金额" />
          </Form.Item>
          <Form.Item
            name="creditAccountBalance"
            label="授信账户余额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入授信账户余额" />
          </Form.Item>
          <Form.Item
            name="totalAccountAmount"
            label="账户总额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入账户总额" />
          </Form.Item>
          <Form.Item
            name="totalAccountConsumed"
            label="账户已消费总金额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入账户已消费总金额" />
          </Form.Item>
          <Form.Item
            name="totalAccountBalance"
            label="账户总余额"
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入账户总余额" />
          </Form.Item>
          <Form.Item
            name="alertThreshold"
            label="差值提醒"
            rules={[{ required: true, message: '请输入差值提醒金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入差值提醒金额，不足此金额将发消息提醒销售" defaultValue={0} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="正常使用" unCheckedChildren="已停用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default CustomerBalanceManagement

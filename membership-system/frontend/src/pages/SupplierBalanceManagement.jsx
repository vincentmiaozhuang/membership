import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, message, Popconfirm, InputNumber } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import request from '../utils/request'

const SupplierBalanceManagement = () => {
  const [supplierBalances, setSupplierBalances] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [currentBalance, setCurrentBalance] = useState(null)
  const [form] = Form.useForm()

  // 获取所有供应商余额
  const fetchSupplierBalances = async () => {
    try {
      setLoading(true)
      const data = await request.get('/supplier-balances')
      setSupplierBalances(data)
    } catch (error) {
      message.error('获取供应商余额失败')
    } finally {
      setLoading(false)
    }
  }

  // 初始化数据
  useEffect(() => {
    fetchSupplierBalances()
  }, [])

  // 打开编辑对话框
  const handleEdit = (record) => {
    setCurrentBalance(record)
    form.setFieldsValue({
      totalRecharge: parseFloat(record.totalRecharge || 0),
      consumedAmount: parseFloat(record.consumedAmount || 0),
      remainingAmount: parseFloat(record.remainingAmount || 0),
      alertThreshold: record.alertThreshold ? parseFloat(record.alertThreshold) : null
    })
    setModalVisible(true)
  }

  // 保存编辑
  const handleOk = async () => {
    try {
      const values = await form.validateFields()
      // 转换数值类型为字符串，以正确处理BigDecimal类型
      const formattedValues = {
        totalRecharge: values.totalRecharge.toString(),
        consumedAmount: values.consumedAmount.toString(),
        remainingAmount: values.remainingAmount.toString(),
        alertThreshold: values.alertThreshold ? values.alertThreshold.toString() : null
      }
      
      if (currentBalance) {
        // 更新现有余额
        await request.put(`/supplier-balances/${currentBalance.id}`, formattedValues)
        message.success('更新余额成功')
      } else {
        // 创建新余额
        await request.post('/supplier-balances', {
          ...formattedValues,
          supplierId: values.supplierId
        })
        message.success('创建余额成功')
      }
      
      setModalVisible(false)
      fetchSupplierBalances()
    } catch (error) {
      message.error('保存失败')
    }
  }

  // 删除余额
  const handleDelete = async (id) => {
    try {
      await request.delete(`/supplier-balances/${id}`)
      message.success('删除成功')
      fetchSupplierBalances()
    } catch (error) {
      message.error('删除失败')
    }
  }

  // 表格列定义
  const columns = [
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      key: 'supplierName',
    },
    {
      title: '供应商编码',
      dataIndex: 'supplierCode',
      key: 'supplierCode',
    },
    {
      title: '账户充值总额',
      dataIndex: 'totalRecharge',
      key: 'totalRecharge',
      render: (text) => `¥${text}`
    },
    {
      title: '已消耗金额',
      dataIndex: 'consumedAmount',
      key: 'consumedAmount',
      render: (text) => `¥${text}`
    },
    {
      title: '剩余金额',
      dataIndex: 'remainingAmount',
      key: 'remainingAmount',
      render: (text) => `¥${text}`
    },
    {
      title: '差值提醒',
      dataIndex: 'alertThreshold',
      key: 'alertThreshold',
      render: (text) => text ? `¥${text}` : '未设置'
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <>
          <Button 
            type="primary" 
            icon={<EditOutlined />} 
            size="small" 
            style={{ marginRight: 8 }}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button 
              danger 
              icon={<DeleteOutlined />} 
              size="small"
            >
              删除
            </Button>
          </Popconfirm>
        </>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>供应商余额管理</h2>
      </div>
      <Table 
        columns={columns} 
        dataSource={supplierBalances} 
        rowKey="id" 
        loading={loading}
        pagination={{ pageSize: 10 }}
      />
      
      {/* 编辑/创建对话框 */}
      <Modal
        title={currentBalance ? '编辑余额' : '创建余额'}
        open={modalVisible}
        onOk={handleOk}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          {!currentBalance && (
            <Form.Item
              name="supplierId"
              label="供应商ID"
              rules={[{ required: true, message: '请输入供应商ID' }]}
            >
              <InputNumber style={{ width: '100%' }} min={1} />
            </Form.Item>
          )}
          <Form.Item
            name="totalRecharge"
            label="账户充值总额"
            rules={[{ required: true, message: '请输入账户充值总额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="consumedAmount"
            label="已消耗金额"
            rules={[{ required: true, message: '请输入已消耗金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="remainingAmount"
            label="剩余金额"
            rules={[{ required: true, message: '请输入剩余金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="alertThreshold"
            label="差值提醒（不足n万发消息）"
          >
            <InputNumber style={{ width: '100%' }} min={0} step={1} placeholder="输入预警金额" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default SupplierBalanceManagement
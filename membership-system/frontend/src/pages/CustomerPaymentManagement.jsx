import { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, InputNumber, message, Popconfirm, Upload } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons'
import request from '../utils/request'

const CustomerPaymentManagement = () => {
  const [customerPayments, setCustomerPayments] = useState([])
  const [customers, setCustomers] = useState([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingCustomerPayment, setEditingCustomerPayment] = useState(null)
  const [financialScreenshot, setFinancialScreenshot] = useState('')
  const [form] = Form.useForm()

  useEffect(() => {
    fetchCustomerPayments()
    fetchCustomers()
  }, [])

  const fetchCustomerPayments = async () => {
    try {
      const data = await request.get('/customer-payments')
      setCustomerPayments(data)
    } catch (error) {
      message.error('获取客户付款列表失败')
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

  const handleAdd = () => {
    setEditingCustomerPayment(null)
    form.resetFields()
    setFinancialScreenshot('')
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingCustomerPayment(record)
    form.setFieldsValue({
      customerId: record.customerId,
      paymentAmount: record.paymentAmount,
      creditAmount: record.creditAmount
    })
    setFinancialScreenshot(record.financialScreenshot || '')
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/customer-payments/${id}`)
      message.success('删除成功')
      fetchCustomerPayments()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const formattedValues = {
        customerId: values.customerId,
        paymentAmount: values.paymentAmount.toString(),
        creditAmount: values.creditAmount.toString(),
        financialScreenshot: financialScreenshot,
        operator: userInfo?.id || 'system'
      }
      
      if (editingCustomerPayment) {
        await request.put(`/customer-payments/${editingCustomerPayment.id}`, formattedValues)
        message.success('更新成功')
      } else {
        await request.post('/customer-payments', formattedValues)
        message.success('创建成功')
      }
      setModalVisible(false)
      setFinancialScreenshot('')
      fetchCustomerPayments()
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
      title: '付款金额',
      dataIndex: 'paymentAmount',
      key: 'paymentAmount',
    },
    {
      title: '授信金额',
      dataIndex: 'creditAmount',
      key: 'creditAmount',
    },
    {
      title: '财务截图',
      dataIndex: 'financialScreenshot',
      key: 'financialScreenshot',
      render: (screenshot) => screenshot ? (
        <a href={`/api${screenshot}`} target="_blank" rel="noopener noreferrer">查看截图</a>
      ) : '-',
    },
    {
      title: '操作人员',
      dataIndex: 'operator',
      key: 'operator',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date) => (date ? new Date(date).toLocaleString() : '-'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (date) => (date ? new Date(date).toLocaleString() : '-'),
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
            title="确定删除此客户付款记录吗？"
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
          新增客户付款
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={customerPayments}
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />
      <Modal
        title={editingCustomerPayment ? '编辑客户付款' : '新增客户付款'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => {
          setModalVisible(false)
          setFinancialScreenshot('')
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="customerId"
            label="客户"
            rules={[{ required: true, message: '请选择客户' }]}
          >
            <Select placeholder="请选择客户">
              {customers.map((customer) => (
                <Select.Option key={customer.id} value={customer.id}>
                  {customer.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="paymentAmount"
            label="付款金额"
            rules={[{ required: true, message: '请输入付款金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入付款金额" />
          </Form.Item>
          <Form.Item
            name="creditAmount"
            label="授信金额"
            rules={[{ required: true, message: '请输入授信金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入授信金额" />
          </Form.Item>
          <Form.Item
            name="financialScreenshot"
            label="财务截图"
          >
            <Upload.Dragger
              name="file"
              action="/api/upload"
              listType="picture"
              maxCount={1}
              onChange={(info) => {
                if (info.file.status === 'done') {
                  setFinancialScreenshot(info.file.response.url)
                }
              }}
            >
              <p className="ant-upload-drag-icon">
                <UploadOutlined />
              </p>
              <p className="ant-upload-text">点击或拖拽文件到此处上传</p>
              <p className="ant-upload-hint">
                支持上传图片文件，单个文件大小不超过5MB
              </p>
            </Upload.Dragger>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default CustomerPaymentManagement

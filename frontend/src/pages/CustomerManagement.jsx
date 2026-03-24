import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Switch, message, Popconfirm, Tabs, Select, DatePicker, Upload, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, GiftOutlined, DollarOutlined, UploadOutlined, WalletOutlined } from '@ant-design/icons'
import request from '../utils/request'
import dayjs from 'dayjs'

const { TabPane } = Tabs

const CustomerManagement = () => {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [assignModalVisible, setAssignModalVisible] = useState(false)
  const [editingCustomer, setEditingCustomer] = useState(null)
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [customerProducts, setCustomerProducts] = useState([])
  const [availableProducts, setAvailableProducts] = useState([])
  const [customerPayments, setCustomerPayments] = useState([])
  const [productModalVisible, setProductModalVisible] = useState(false)
  const [paymentModalVisible, setPaymentModalVisible] = useState(false)
  const [balanceModalVisible, setBalanceModalVisible] = useState(false)
  const [customerBalance, setCustomerBalance] = useState(null)
  const [addPaymentModalVisible, setAddPaymentModalVisible] = useState(false)
  const [paymentForm] = Form.useForm()
  const [balanceForm] = Form.useForm()
  const [financialScreenshot, setFinancialScreenshot] = useState('')
  const [form] = Form.useForm()
  const [assignForm] = Form.useForm()

  const fetchCustomers = async () => {
    setLoading(true)
    try {
      const data = await request.get('/customers')
      setCustomers(data)
    } catch (error) {
      message.error('获取客户列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchCustomerProducts = async (customerId) => {
    try {
      const data = await request.get(`/customers/${customerId}/products`)
      setCustomerProducts(data)
    } catch (error) {
      message.error('获取客户产品失败')
    }
  }

  const fetchAvailableProducts = async () => {
    try {
      const data = await request.get('/products/enabled')
      setAvailableProducts(data)
    } catch (error) {
      message.error('获取产品列表失败')
    }
  }

  const fetchCustomerPayments = async (customerId) => {
    try {
      const data = await request.get(`/customer-payments/customer/${customerId}`)
      setCustomerPayments(data)
    } catch (error) {
      message.error('获取客户付款记录失败')
    }
  }

  const fetchCustomerBalance = async (customerId) => {
    try {
      const data = await request.get(`/customer-balances/customer/${customerId}`)
      setCustomerBalance(data)
      balanceForm.setFieldsValue({
        paymentAccountTotal: data.paymentAccountTotal || 0,
        paymentAccountConsumed: data.paymentAccountConsumed || 0,
        creditAccountTotal: data.creditAccountTotal || 0,
        creditAccountConsumed: data.creditAccountConsumed || 0,
        totalAccountAmount: data.totalAccountAmount || 0,
        totalAccountConsumed: data.totalAccountConsumed || 0,
        alertThreshold: data.alertThreshold || 0,
        enabled: data.enabled !== undefined ? data.enabled : true,
      })
    } catch (error) {
      message.error('获取客户余额失败')
      setCustomerBalance(null)
    }
  }

  useEffect(() => {
    fetchCustomers()
    fetchAvailableProducts()
  }, [])

  const handleAdd = () => {
    setEditingCustomer(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingCustomer(record)
    form.setFieldsValue({
      name: record.name,
      ipWhitelist: record.ipWhitelist,
      cooperationStartDate: record.cooperationStartDate ? dayjs(record.cooperationStartDate) : null,
      enabled: record.enabled,
      createdAt: record.createdAt ? new Date(record.createdAt).toLocaleString() : '-',
      updatedAt: record.updatedAt ? new Date(record.updatedAt).toLocaleString() : '-'
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/customers/${id}`)
      message.success('删除成功')
      fetchCustomers()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleBalanceUpdate = async () => {
    try {
      if (!customerBalance) {
        message.error('客户余额信息不存在')
        return
      }
      const values = await balanceForm.validateFields()
      await request.put(`/customer-balances/${customerBalance.id}`, {
        paymentAccountTotal: values.paymentAccountTotal,
        paymentAccountConsumed: values.paymentAccountConsumed,
        creditAccountTotal: values.creditAccountTotal,
        creditAccountConsumed: values.creditAccountConsumed,
        totalAccountAmount: values.totalAccountAmount,
        totalAccountConsumed: values.totalAccountConsumed,
        alertThreshold: values.alertThreshold,
        enabled: values.enabled,
      })
      message.success('更新余额成功')
      setBalanceModalVisible(false)
      setCustomerBalance(null)
      setSelectedCustomer(null)
    } catch (error) {
      message.error('更新余额失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      // 排除createdAt和updatedAt字段，因为这些字段由JPA自动管理
      const { createdAt, updatedAt, ...submitValues } = values
      if (editingCustomer) {
        await request.put(`/customers/${editingCustomer.id}`, submitValues)
        message.success('更新成功')
      } else {
        await request.post('/customers', submitValues)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchCustomers()
    } catch (error) {
      console.error(error)
    }
  }

  const handleAssignProduct = (customer) => {
    setSelectedCustomer(customer)
    assignForm.resetFields()
    setAssignModalVisible(true)
  }

  const handleAssignOk = async () => {
    try {
      const values = await assignForm.validateFields()
      const stockAmount = values.customerPrice * values.stockQuantity
      await request.post(`/customers/${selectedCustomer.id}/products`, {
        productId: values.productId,
        customerPrice: values.customerPrice.toString(),
        stockQuantity: values.stockQuantity,
        stockAmount: stockAmount.toString()
      })
      message.success('分配成功')
      setAssignModalVisible(false)
      fetchCustomerProducts(selectedCustomer.id)
    } catch (error) {
      console.error(error)
    }
  }

  const handleRemoveProduct = async (customerId, customerProductId) => {
    try {
      await request.delete(`/customers/${customerId}/products/${customerProductId}`)
      message.success('移除成功')
      fetchCustomerProducts(customerId)
    } catch (error) {
      message.error('移除失败')
    }
  }

  const customerColumns = [
    {
      title: '客户名称',
      dataIndex: 'name',
      key: 'name',
      width: 120,
    },
    {
      title: '客户代码',
      dataIndex: 'customerCode',
      key: 'customerCode',
    },
    {
      title: '客户秘钥',
      dataIndex: 'customerSecret',
      key: 'customerSecret',
    },
    {
      title: '客户IP白名单',
      dataIndex: 'ipWhitelist',
      key: 'ipWhitelist',
    },
    {
      title: '合作开始时间',
      dataIndex: 'cooperationStartDate',
      key: 'cooperationStartDate',
      render: (date) => (date ? new Date(date).toLocaleString() : '-'),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled) => (enabled ? '启用' : '禁用'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <span>
          <Button
            type="link"
            icon={<GiftOutlined />}
            onClick={() => {
              setSelectedCustomer(record)
              fetchCustomerProducts(record.id)
              setProductModalVisible(true)
            }}
          >
            产品
          </Button>
          <Button
            type="link"
            icon={<WalletOutlined />}
            onClick={() => {
              setSelectedCustomer(record)
              fetchCustomerBalance(record.id)
              setBalanceModalVisible(true)
            }}
          >
            余额
          </Button>
          <Button
            type="link"
            icon={<DollarOutlined />}
            onClick={() => {
              setSelectedCustomer(record)
              fetchCustomerPayments(record.id)
              setPaymentModalVisible(true)
            }}
          >
            付款
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此客户吗？"
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

  const customerProductColumns = [
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
      width: 120,
    },
    {
      title: '产品码',
      dataIndex: 'productCode',
      key: 'productCode',
      width: 100,
    },
    {
      title: '产品类型',
      dataIndex: 'productType',
      key: 'productType',
      width: 80,
    },
    {
      title: '面值',
      dataIndex: 'faceValue',
      key: 'faceValue',
      width: 60,
    },
    {
      title: '客户产品代码',
      dataIndex: 'customerProductCode',
      key: 'customerProductCode',
      width: 120,
    },
    {
      title: '客户价格',
      dataIndex: 'customerPrice',
      key: 'customerPrice',
      width: 80,
    },
    {
      title: '库存量',
      dataIndex: 'stockQuantity',
      key: 'stockQuantity',
      width: 80,
    },
    {
      title: '出货量',
      dataIndex: 'shipmentQuantity',
      key: 'shipmentQuantity',
      width: 80,
    },
    {
      title: '库存金额',
      dataIndex: 'stockAmount',
      key: 'stockAmount',
      width: 80,
    },
    {
      title: '出货金额',
      dataIndex: 'shipmentAmount',
      key: 'shipmentAmount',
      width: 80,
    },
    {
      title: '操作',
      key: 'action',
      width: 60,
      render: (_, record) => (
        <Popconfirm
          title="确定移除此产品吗？"
          onConfirm={() => handleRemoveProduct(selectedCustomer.id, record.id)}
        >
          <Button type="link" danger icon={<DeleteOutlined />}>
            移除
          </Button>
        </Popconfirm>
      ),
    },
  ]

  const customerPaymentColumns = [
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
  ]

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增客户
        </Button>
      </div>
      <Table
        columns={customerColumns}
        dataSource={customers}
        rowKey="id"
        loading={loading}
      />
      <Modal
        title={editingCustomer ? '编辑客户' : '新增客户'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="客户名称"
            rules={[{ required: true, message: '请输入客户名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="ipWhitelist"
            label="客户IP白名单"
          >
            <Input placeholder="多个IP用逗号分隔" />
          </Form.Item>
          <Form.Item
            name="cooperationStartDate"
            label="合作开始时间"
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
          <Form.Item
            name="createdAt"
            label="创建时间"
          >
            <Input disabled placeholder="创建时间" />
          </Form.Item>
          <Form.Item
            name="updatedAt"
            label="修改时间"
          >
            <Input disabled placeholder="修改时间" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={`客户产品 - ${selectedCustomer?.name}`}
        open={productModalVisible}
        onCancel={() => {
          setProductModalVisible(false)
          setSelectedCustomer(null)
        }}
        footer={null}
        width={1000}
      >
        <Tabs defaultActiveKey="products">
          <TabPane tab="已分配产品" key="products">
            <div style={{ marginBottom: 16 }}>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => handleAssignProduct(selectedCustomer)}
              >
                分配产品
              </Button>
            </div>
            <div style={{ overflowX: 'auto' }}>
              <Table
                columns={customerProductColumns}
                dataSource={customerProducts}
                rowKey="id"
                pagination={false}
                scroll={{ x: 'max-content' }}
              />
            </div>
          </TabPane>
        </Tabs>
      </Modal>
      <Modal
        title={`客户付款 - ${selectedCustomer?.name}`}
        open={paymentModalVisible}
        onCancel={() => {
          setPaymentModalVisible(false)
          setSelectedCustomer(null)
          setCustomerPayments([])
        }}
        footer={null}
        width={800}
      >
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              paymentForm.resetFields()
              setAddPaymentModalVisible(true)
            }}
          >
            新增付款
          </Button>
        </div>
        <Table
          columns={customerPaymentColumns}
          dataSource={customerPayments}
          rowKey="id"
          pagination={false}
        />
      </Modal>
      <Modal
        title="新增付款"
        open={addPaymentModalVisible}
        onOk={async () => {
          try {
            const values = await paymentForm.validateFields()
            await request.post('/customer-payments', {
              customerId: selectedCustomer.id,
              paymentAmount: values.paymentAmount.toString(),
              creditAmount: values.creditAmount.toString(),
              financialScreenshot: financialScreenshot
            })
            message.success('新增付款成功')
            setAddPaymentModalVisible(false)
            setFinancialScreenshot('')
            fetchCustomerPayments(selectedCustomer.id)
          } catch (error) {
            console.error(error)
          }
        }}
        onCancel={() => {
          setAddPaymentModalVisible(false)
          setFinancialScreenshot('')
        }}
      >
        <Form form={paymentForm} layout="vertical">
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
                支持上传图片文件，单个文件大小不超过5MB（非必填）
              </p>
            </Upload.Dragger>
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={`客户余额 - ${selectedCustomer?.name}`}
        open={balanceModalVisible}
        onCancel={() => {
          setBalanceModalVisible(false)
          setSelectedCustomer(null)
          setCustomerBalance(null)
        }}
        footer={null}
        width={1000}
      >
        {customerBalance ? (
          <Form form={balanceForm} layout="vertical">
            <div style={{ marginBottom: 24 }}>
              <div style={{ display: 'flex', marginBottom: 8, fontWeight: 'bold' }}>
                <div style={{ width: '20%' }}>账户类型</div>
                <div style={{ width: '25%' }}>总额</div>
                <div style={{ width: '25%' }}>已消费金额</div>
                <div style={{ width: '30%' }}>余额</div>
              </div>
              <div style={{ display: 'flex', marginBottom: 12, alignItems: 'center' }}>
                <div style={{ width: '20%' }}>付款账户</div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="paymentAccountTotal"
                    noStyle
                    rules={[{ required: true, message: '请输入付款账户总额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入总额" />
                  </Form.Item>
                </div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="paymentAccountConsumed"
                    noStyle
                    rules={[{ required: true, message: '请输入付款账户已消费金额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入已消费金额" />
                  </Form.Item>
                </div>
                <div style={{ width: '30%' }}>¥{customerBalance.paymentAccountBalance ? customerBalance.paymentAccountBalance.toFixed(2) : '0.00'}</div>
              </div>
              <div style={{ display: 'flex', marginBottom: 12, alignItems: 'center' }}>
                <div style={{ width: '20%' }}>授信账户</div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="creditAccountTotal"
                    noStyle
                    rules={[{ required: true, message: '请输入授信账户总额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入总额" />
                  </Form.Item>
                </div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="creditAccountConsumed"
                    noStyle
                    rules={[{ required: true, message: '请输入授信账户已消费金额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入已消费金额" />
                  </Form.Item>
                </div>
                <div style={{ width: '30%' }}>¥{customerBalance.creditAccountBalance ? customerBalance.creditAccountBalance.toFixed(2) : '0.00'}</div>
              </div>
              <div style={{ display: 'flex', marginBottom: 12, alignItems: 'center' }}>
                <div style={{ width: '20%' }}>账户总计</div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="totalAccountAmount"
                    noStyle
                    rules={[{ required: true, message: '请输入账户总计总额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入总额" />
                  </Form.Item>
                </div>
                <div style={{ width: '25%' }}>
                  <Form.Item
                    name="totalAccountConsumed"
                    noStyle
                    rules={[{ required: true, message: '请输入账户总计已消费金额' }]}
                  >
                    <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入已消费金额" />
                  </Form.Item>
                </div>
                <div style={{ width: '30%' }}>¥{customerBalance.totalAccountBalance ? customerBalance.totalAccountBalance.toFixed(2) : '0.00'}</div>
              </div>
            </div>
            <Form.Item
              name="alertThreshold"
              label="差值提醒"
              rules={[{ required: true, message: '请输入差值提醒金额' }]}
            >
              <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入差值提醒金额，不足此金额将发消息提醒销售" />
            </Form.Item>
            <Form.Item
              name="enabled"
              label="状态"
              rules={[{ required: true, message: '请选择状态' }]}
            >
              <Select placeholder="请选择状态">
                <Select.Option value={true}>正常使用</Select.Option>
                <Select.Option value={false}>已停用</Select.Option>
              </Select>
            </Form.Item>
            <div style={{ textAlign: 'right', marginTop: 16 }}>
              <Button onClick={() => {
                setBalanceModalVisible(false)
                setSelectedCustomer(null)
                setCustomerBalance(null)
              }} style={{ marginRight: 8 }}>
                取消
              </Button>
              <Button type="primary" onClick={handleBalanceUpdate}>
                修改
              </Button>
            </div>
          </Form>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <p style={{ color: '#999' }}>该客户暂无余额记录</p>
          </div>
        )}
      </Modal>
      <Modal
        title="分配产品"
        open={assignModalVisible}
        onOk={handleAssignOk}
        onCancel={() => setAssignModalVisible(false)}
      >
        <Form form={assignForm} layout="vertical">
          <Form.Item
            name="productId"
            label="产品"
            rules={[{ required: true, message: '请选择产品' }]}
          >
            <Select placeholder="请选择产品">
              {availableProducts.map((product) => (
                <Select.Option key={product.id} value={product.id}>
                  {product.name} - {product.type}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="customerPrice"
            label="客户价格"
            rules={[{ required: true, message: '请输入客户价格' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入客户价格" />
          </Form.Item>
          <Form.Item
            name="stockQuantity"
            label="库存量"
            rules={[{ required: true, message: '请输入库存量' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入库存量" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default CustomerManagement

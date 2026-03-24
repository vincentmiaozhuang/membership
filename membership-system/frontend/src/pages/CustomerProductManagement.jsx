import { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, InputNumber, Switch, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const CustomerProductManagement = () => {
  const [customerProducts, setCustomerProducts] = useState([])
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingCustomerProduct, setEditingCustomerProduct] = useState(null)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchCustomerProducts()
    fetchCustomers()
    fetchProducts()
  }, [])

  const fetchCustomerProducts = async () => {
    try {
      const data = await request.get('/customer-products')
      setCustomerProducts(data)
    } catch (error) {
      message.error('获取客户产品列表失败')
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
  
  const fetchProducts = async () => {
    try {
      const data = await request.get('/products/enabled')
      setProducts(data)
    } catch (error) {
      message.error('获取产品列表失败')
    }
  }

  const handleAdd = () => {
    setEditingCustomerProduct(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingCustomerProduct(record)
    form.setFieldsValue({
      customerId: record.customerId,
      productId: record.productId,
      customerPrice: record.customerPrice,
      stockQuantity: record.stockQuantity,
      shipmentQuantity: record.shipmentQuantity,
      stockAmount: record.stockAmount,
      shipmentAmount: record.shipmentAmount,
      enabled: record.enabled
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/customer-products/${id}`)
      message.success('删除成功')
      fetchCustomerProducts()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      const formattedValues = {
        customerId: values.customerId,
        productId: values.productId,
        customerPrice: values.customerPrice.toString(),
        stockQuantity: values.stockQuantity,
        shipmentQuantity: values.shipmentQuantity,
        stockAmount: values.stockAmount.toString(),
        shipmentAmount: values.shipmentAmount.toString(),
        enabled: values.enabled !== undefined ? values.enabled : true
      }
      
      if (editingCustomerProduct) {
        await request.put(`/customer-products/${editingCustomerProduct.id}`, formattedValues)
        message.success('更新成功')
      } else {
        await request.post('/customer-products', formattedValues)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchCustomerProducts()
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
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '客户产品代码',
      dataIndex: 'customerProductCode',
      key: 'customerProductCode',
    },
    {
      title: '客户价格',
      dataIndex: 'customerPrice',
      key: 'customerPrice',
    },
    {
      title: '库存量',
      dataIndex: 'stockQuantity',
      key: 'stockQuantity',
    },
    {
      title: '出货量',
      dataIndex: 'shipmentQuantity',
      key: 'shipmentQuantity',
    },
    {
      title: '库存金额',
      dataIndex: 'stockAmount',
      key: 'stockAmount',
    },
    {
      title: '出货金额',
      dataIndex: 'shipmentAmount',
      key: 'shipmentAmount',
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled) => (enabled ? '启用' : '禁用'),
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
            title="确定删除此客户产品吗？"
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
          新增客户产品
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={customerProducts}
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />
      <Modal
        title={editingCustomerProduct ? '编辑客户产品' : '新增客户产品'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
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
            name="productId"
            label="产品"
            rules={[{ required: true, message: '请选择产品' }]}
          >
            <Select placeholder="请选择产品">
              {products.map((product) => (
                <Select.Option key={product.id} value={product.id}>
                  {product.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="customerPrice"
            label="客户价格"
            rules={[{ required: true, message: '请输入客户价格' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="stockQuantity"
            label="库存量"
            rules={[{ required: true, message: '请输入库存量' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="shipmentQuantity"
            label="出货量"
            rules={[{ required: true, message: '请输入出货量' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="stockAmount"
            label="库存金额"
            rules={[{ required: true, message: '请输入库存金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="shipmentAmount"
            label="出货金额"
            rules={[{ required: true, message: '请输入出货金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default CustomerProductManagement
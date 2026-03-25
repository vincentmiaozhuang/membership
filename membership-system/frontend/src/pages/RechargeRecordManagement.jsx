import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm, DatePicker } from 'antd'
import { PlusOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons'
import request from '../utils/request'

const RechargeRecordManagement = () => {
  const [records, setRecords] = useState([])
  const [products, setProducts] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [relatedSuppliers, setRelatedSuppliers] = useState([])
  const [customers, setCustomers] = useState([])
  const [allCustomers, setAllCustomers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [searchRechargePhone, setSearchRechargePhone] = useState('')
  const [searchStatus, setSearchStatus] = useState('')
  const [searchProductId, setSearchProductId] = useState('')
  const [searchCustomerId, setSearchCustomerId] = useState('')
  const [searchSupplierId, setSearchSupplierId] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(50)
  const [form] = Form.useForm()

  const fetchRecords = async () => {
    setLoading(true)
    try {
      const data = await request.get('/recharge-records')
      const sortedData = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setRecords(sortedData)
    } catch (error) {
      message.error('获取充值记录失败')
    } finally {
      setLoading(false)
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

  const fetchSuppliers = async () => {
    try {
      const data = await request.get('/suppliers/enabled')
      setSuppliers(data)
    } catch (error) {
      message.error('获取供应商列表失败')
    }
  }

  const fetchAllCustomers = async () => {
    try {
      const data = await request.get('/customers/enabled')
      setAllCustomers(data)
    } catch (error) {
      message.error('获取客户列表失败')
    }
  }

  const fetchCustomersByProductId = async (productId) => {
    try {
      const data = await request.get(`/customer-products/product/${productId}`)
      // 去重，确保每个客户只出现一次
      const uniqueCustomers = []
      const customerIds = new Set()
      data.forEach(item => {
        if (!customerIds.has(item.customerId)) {
          customerIds.add(item.customerId)
          uniqueCustomers.push({
            id: item.customerId,
            name: item.customerName,
            price: item.customerPrice
          })
        }
      })
      setCustomers(uniqueCustomers)
    } catch (error) {
      message.error('获取客户列表失败')
    }
  }

  const fetchSuppliersByProductId = async (productId) => {
    try {
      const data = await request.get(`/supplier-products/product/${productId}`)
      // 去重，确保每个供应商只出现一次
      const uniqueSuppliers = []
      const supplierIds = new Set()
      data.forEach(item => {
        if (!supplierIds.has(item.supplierId)) {
          supplierIds.add(item.supplierId)
          uniqueSuppliers.push({
            id: item.supplierId,
            name: item.supplierName,
            price: item.supplierPrice
          })
        }
      })
      setRelatedSuppliers(uniqueSuppliers)
    } catch (error) {
      message.error('获取供应商列表失败')
    }
  }

  useEffect(() => {
    fetchRecords()
    fetchProducts()
    fetchSuppliers()
    fetchAllCustomers()
  }, [])

  const handleAdd = () => {
    form.resetFields()
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/recharge-records/${id}`)
      message.success('删除成功')
      fetchRecords()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      // 设置充值人默认为 0
      values.rechargePerson = '0'
      await request.post('/recharge-records', values)
      message.success('充值记录创建成功')
      setModalVisible(false)
      fetchRecords()
    } catch (error) {
      console.error(error)
    }
  }

  const handleSearch = async () => {
    setLoading(true)
    try {
      let data = await request.get('/recharge-records')
      
      // 按充值手机号查询（必须输入完整手机号）
      if (searchRechargePhone) {
        data = data.filter(record => record.rechargePhone === searchRechargePhone)
      }
      
      // 按产品查询
      if (searchProductId) {
        data = data.filter(record => record.productId === parseInt(searchProductId))
      }
      
      // 按客户查询
      if (searchCustomerId) {
        data = data.filter(record => record.customerId === parseInt(searchCustomerId))
      }
      
      // 按供应商查询
      if (searchSupplierId) {
        data = data.filter(record => record.supplierId === parseInt(searchSupplierId))
      }
      
      // 按状态查询
      if (searchStatus) {
        data = data.filter(record => record.status === searchStatus)
      }
      
      const sortedData = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setRecords(sortedData)
    } catch (error) {
      message.error('查询失败')
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    setSearchRechargePhone('')
    setSearchStatus('')
    setSearchProductId('')
    setSearchCustomerId('')
    setSearchSupplierId('')
    fetchRecords()
  }

  const columns = [
    {
      title: '充值手机号',
      dataIndex: 'rechargePhone',
      key: 'rechargePhone',
      width: 120,
    },
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
      width: 120,
    },
    {
      title: '平台订单ID',
      dataIndex: 'platformOrderId',
      key: 'platformOrderId',
      width: 120,
    },
    {
      title: '客户订单ID',
      dataIndex: 'customerOrderId',
      key: 'customerOrderId',
      width: 120,
    },
    {
      title: '产品票面价格',
      dataIndex: 'productFacePrice',
      key: 'productFacePrice',
      width: 100,
    },
    {
      title: '客户名称',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 100,
    },
    {
      title: '客户价格',
      dataIndex: 'customerPrice',
      key: 'customerPrice',
      width: 80,
    },
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      key: 'supplierName',
      width: 100,
    },
    {
      title: '供应商价格',
      dataIndex: 'supplierPrice',
      key: 'supplierPrice',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status) => {
        const color = status === '成功' ? 'green' : status === '失败' ? 'red' : 'orange'
        return <span style={{ color }}>{status}</span>
      },
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 150,
    },
    {
      title: '充值人',
      dataIndex: 'rechargePerson',
      key: 'rechargePerson',
      width: 100,
    },
    {
      title: '充值时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date) => (date ? date.replace('T', ' ') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_, record) => (
        <Popconfirm
          title="确定删除此充值记录吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ]

  return (
    <div>
      <div style={{ 
        marginBottom: 20, 
        padding: 16, 
        backgroundColor: '#f7f9fc', 
        border: '1px solid #e8e8e8', 
        borderRadius: 8, 
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.09)',
        display: 'flex', 
        gap: 16, 
        alignItems: 'center', 
        flexWrap: 'wrap'
      }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          手动充值
        </Button>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>充值手机号：</span>
          <Input
            placeholder="请输入完整手机号"
            value={searchRechargePhone}
            onChange={(e) => setSearchRechargePhone(e.target.value)}
            style={{ width: 160, borderRadius: 4, height: 32 }}
          />
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>状态：</span>
          <Select
            placeholder="请选择状态"
            value={searchStatus}
            onChange={setSearchStatus}
            style={{ width: 110, borderRadius: 4 }}
            allowClear
          >
            <Select.Option value="成功">成功</Select.Option>
            <Select.Option value="失败">失败</Select.Option>
            <Select.Option value="充值中">充值中</Select.Option>
          </Select>
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>产品：</span>
          <Select
            placeholder="请选择产品"
            value={searchProductId}
            onChange={setSearchProductId}
            style={{ width: 130, borderRadius: 4 }}
            allowClear
          >
            {products.map((product) => (
              <Select.Option key={product.id} value={product.id}>
                {product.name}
              </Select.Option>
            ))}
          </Select>
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>客户：</span>
          <Select
            placeholder="请选择客户"
            value={searchCustomerId}
            onChange={setSearchCustomerId}
            style={{ width: 130, borderRadius: 4 }}
            allowClear
          >
            {allCustomers.map((customer) => (
              <Select.Option key={customer.id} value={customer.id}>
                {customer.name}
              </Select.Option>
            ))}
          </Select>
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>供应商：</span>
          <Select
            placeholder="请选择供应商"
            value={searchSupplierId}
            onChange={setSearchSupplierId}
            style={{ width: 130, borderRadius: 4 }}
            allowClear
          >
            {suppliers.map((supplier) => (
              <Select.Option key={supplier.id} value={supplier.id}>
                {supplier.name}
              </Select.Option>
            ))}
          </Select>
        </div>
        
        <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} style={{ marginLeft: 8 }}>
          查询
        </Button>
        
        <Button onClick={handleReset} style={{ marginLeft: 8 }}>
          重置
        </Button>
      </div>
      <div style={{ overflowX: 'auto' }}>
        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          scroll={{ x: 'max-content' }}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: records.length,
            onChange: (page, size) => {
              setCurrentPage(page)
              setPageSize(size)
            },
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50', '100'],
            defaultPageSize: 50
          }}
        />
      </div>
      <Modal
        title="新增充值记录"
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="rechargePhone"
            label="充值手机号"
            rules={[
              { required: true, message: '请输入充值手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入11位有效的手机号码' }
            ]}
          >
            <Input placeholder="请输入充值手机号" />
          </Form.Item>
          <Form.Item
            name="productName"
            label="产品名称"
            rules={[{ required: true, message: '请选择产品名称' }]}
          >
            <Select 
              placeholder="请选择产品名称"
              onChange={(value) => {
                // 查找选择的产品
                const selectedProduct = products.find(p => p.name === value)
                if (selectedProduct) {
                  // 设置产品ID和产品票面价格
                  form.setFieldsValue({
                    productId: selectedProduct.id,
                    productFacePrice: selectedProduct.faceValue
                  })
                  // 根据产品ID获取客户列表
                  fetchCustomersByProductId(selectedProduct.id)
                  // 根据产品ID获取关联的供应商列表
                  fetchSuppliersByProductId(selectedProduct.id)
                }
              }}
            >
              {products.map((product) => (
                <Select.Option key={product.id} value={product.name}>
                  {product.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="customerName"
            label="客户名称"
            rules={[{ required: true, message: '请选择客户名称' }]}
          >
            <Select 
              placeholder="请选择客户名称"
              onChange={(value) => {
                // 查找选择的客户
                const selectedCustomer = customers.find(c => c.name === value)
                if (selectedCustomer) {
                  // 设置客户ID和客户价格
                  form.setFieldsValue({
                    customerId: selectedCustomer.id,
                    customerPrice: selectedCustomer.price
                  })
                }
              }}
            >
              {customers.map((customer) => (
                <Select.Option key={customer.id} value={customer.name}>
                  {customer.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="supplierName"
            label="供应商名称"
            rules={[{ required: true, message: '请选择供应商名称' }]}
          >
            <Select 
              placeholder="请选择供应商名称"
              onChange={(value) => {
                // 查找选择的供应商
                const selectedSupplier = relatedSuppliers.find(s => s.name === value)
                if (selectedSupplier) {
                  // 设置供应商ID和供应商价格
                  form.setFieldsValue({
                    supplierId: selectedSupplier.id,
                    supplierPrice: selectedSupplier.price
                  })
                }
              }}
            >
              {relatedSuppliers.map((supplier) => (
                <Select.Option key={supplier.id} value={supplier.name}>
                  {supplier.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="status"
            label="充值状态"
            rules={[{ required: true, message: '请选择充值状态' }]}
          >
            <Select placeholder="请选择充值状态">
              <Select.Option value="成功">成功</Select.Option>
              <Select.Option value="失败">失败</Select.Option>
              <Select.Option value="充值中">充值中</Select.Option>
            </Select>
          </Form.Item>
          {/* 隐藏字段，用于存储关联信息 */}
          <Form.Item name="productId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="productFacePrice" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="customerId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="customerPrice" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="customerProductId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="supplierId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="supplierPrice" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="rechargePerson" hidden>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RechargeRecordManagement

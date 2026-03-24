import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, Switch, message, Popconfirm, InputNumber } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const { Option } = Select

const SupplierProductManagement = () => {
  const [supplierProducts, setSupplierProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingSupplierProduct, setEditingSupplierProduct] = useState(null)
  const [form] = Form.useForm()
  const [products, setProducts] = useState([])
  const [suppliers, setSuppliers] = useState([])

  const fetchSupplierProducts = async () => {
    setLoading(true)
    try {
      const data = await request.get('/supplier-products')
      setSupplierProducts(data)
    } catch (error) {
      message.error('获取供应商产品列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchProducts = async () => {
    try {
      const data = await request.get('/products')
      setProducts(data)
    } catch (error) {
      message.error('获取产品列表失败')
    }
  }

  const fetchSuppliers = async () => {
    try {
      const data = await request.get('/suppliers')
      setSuppliers(data)
    } catch (error) {
      message.error('获取供应商列表失败')
    }
  }

  useEffect(() => {
    fetchSupplierProducts()
    fetchProducts()
    fetchSuppliers()
  }, [])

  const handleAdd = () => {
    setEditingSupplierProduct(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingSupplierProduct(record)
    form.setFieldsValue({
      productId: record.productId,
      supplierId: record.supplierId,
      supplierPrice: record.supplierPrice,
      stockQuantity: record.stockQuantity,
      salesQuantity: record.salesQuantity,
      stockAmount: record.stockAmount,
      salesAmount: record.salesAmount,
      enabled: record.enabled
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/supplier-products/${id}`)
      message.success('删除成功')
      fetchSupplierProducts()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      
      if (editingSupplierProduct) {
        // 编辑供应商产品
        await request.put(`/supplier-products/${editingSupplierProduct.id}`, values)
        message.success('编辑成功')
      } else {
        // 新增供应商产品
        await request.post('/supplier-products', values)
        message.success('新增成功')
      }
      
      setModalVisible(false)
      fetchSupplierProducts()
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    }
  }

  const columns = [
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '产品码',
      dataIndex: 'productCode',
      key: 'productCode',
    },
    {
      title: '产品类型',
      dataIndex: 'productType',
      key: 'productType',
    },
    {
      title: '产品面值',
      dataIndex: 'productFaceValue',
      key: 'productFaceValue',
    },
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      key: 'supplierName',
    },
    {
      title: '供应商代码',
      dataIndex: 'supplierCode',
      key: 'supplierCode',
    },
    {
      title: '供应商价格',
      dataIndex: 'supplierPrice',
      key: 'supplierPrice',
    },
    {
      title: '库存量',
      dataIndex: 'stockQuantity',
      key: 'stockQuantity',
    },
    {
      title: '出货量',
      dataIndex: 'salesQuantity',
      key: 'salesQuantity',
    },
    {
      title: '库存金额',
      dataIndex: 'stockAmount',
      key: 'stockAmount',
    },
    {
      title: '出货金额',
      dataIndex: 'salesAmount',
      key: 'salesAmount',
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
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此供应商产品吗？"
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
          新增供应商产品
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={supplierProducts}
        rowKey="id"
        loading={loading}
      />
      <Modal
        title={editingSupplierProduct ? '编辑供应商产品' : '新增供应商产品'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={800}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="productId"
            label="产品"
            rules={[{ required: true, message: '请选择产品' }]}
          >
            <Select style={{ width: '100%' }}>
              {products.map(product => (
                <Option key={product.id} value={product.id}>
                  {product.name} - {product.productCode}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="supplierId"
            label="供应商"
            rules={[{ required: true, message: '请选择供应商' }]}
          >
            <Select style={{ width: '100%' }}>
              {suppliers.map(supplier => (
                <Option key={supplier.id} value={supplier.id}>
                  {supplier.name} - {supplier.supplierCode}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="supplierPrice"
            label="供应商价格"
            rules={[{ required: true, message: '请输入供应商价格' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="stockQuantity"
            label="库存量"
            rules={[{ required: true, message: '请输入库存量' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item
            name="salesQuantity"
            label="出货量"
            rules={[{ required: true, message: '请输入出货量' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item
            name="stockAmount"
            label="库存金额"
            rules={[{ required: true, message: '请输入库存金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="salesAmount"
            label="出货金额"
            rules={[{ required: true, message: '请输入出货金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default SupplierProductManagement
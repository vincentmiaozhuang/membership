import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, Switch, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const ProductManagement = () => {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)
  const [form] = Form.useForm()

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const data = await request.get('/products')
      setProducts(data)
    } catch (error) {
      message.error('获取产品列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [])

  const handleAdd = () => {
    setEditingProduct(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingProduct(record)
    form.setFieldsValue({
      ...record
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/products/${id}`)
      message.success('删除成功')
      fetchProducts()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      if (editingProduct) {
        await request.put(`/products/${editingProduct.id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/products', values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchProducts()
    } catch (error) {
      console.error(error)
    }
  }

  const columns = [
    {
      title: '产品名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '产品码',
      dataIndex: 'productCode',
      key: 'productCode',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '面值',
      dataIndex: 'faceValue',
      key: 'faceValue',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
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
            title="确定删除此产品吗？"
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
          新增产品
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={products}
        rowKey="id"
        loading={loading}
      />
      <Modal
        title={editingProduct ? '编辑产品' : '新增产品'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
        style={{ top: 20 }}
        bodyStyle={{ maxHeight: '80vh', overflow: 'auto' }}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="产品名称"
            rules={[{ required: true, message: '请输入产品名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="productCode"
            label="产品码"
          >
            <Input disabled placeholder="系统自动生成" />
          </Form.Item>
          <Form.Item
            name="type"
            label="类型"
            rules={[{ required: true, message: '请输入类型' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="faceValue"
            label="面值"
            rules={[{ required: true, message: '请输入面值' }]}
          >
            <Input type="number" placeholder="请输入面值" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProductManagement

import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Switch, message, Popconfirm, DatePicker, Select, InputNumber, Upload } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ProductOutlined, TransactionOutlined, WalletOutlined } from '@ant-design/icons'
import moment from 'moment'
import request from '../utils/request'

const { Option } = Select

const SupplierManagement = () => {
  const [suppliers, setSuppliers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingSupplier, setEditingSupplier] = useState(null)
  const [form] = Form.useForm()
  
  // 新增状态
  const [productModalVisible, setProductModalVisible] = useState(false)
  const [currentSupplier, setCurrentSupplier] = useState(null)
  const [supplierProducts, setSupplierProducts] = useState([])
  const [products, setProducts] = useState([])
  const [addProductModalVisible, setAddProductModalVisible] = useState(false)
  const [addProductForm] = Form.useForm()
  
  // 充值相关状态
  const [rechargeModalVisible, setRechargeModalVisible] = useState(false)
  const [supplierRecharges, setSupplierRecharges] = useState([])
  const [addRechargeModalVisible, setAddRechargeModalVisible] = useState(false)
  const [addRechargeForm] = Form.useForm()
  
  // 余额管理相关状态
  const [balanceModalVisible, setBalanceModalVisible] = useState(false)
  const [supplierBalance, setSupplierBalance] = useState(null)
  const [balanceForm] = Form.useForm()

  const fetchSuppliers = async () => {
    setLoading(true)
    try {
      const data = await request.get('/suppliers')
      setSuppliers(data)
    } catch (error) {
      message.error('获取供应商列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSuppliers()
  }, [])

  // 新增函数
  const handleProductClick = async (supplier) => {
    setCurrentSupplier(supplier)
    // 获取供应商关联的产品
    await fetchSupplierProducts(supplier.id)
    // 打开产品对话框
    setProductModalVisible(true)
  }

  const fetchSupplierProducts = async (supplierId) => {
    try {
      const data = await request.get('/supplier-products')
      // 过滤出当前供应商的产品
      const filteredProducts = data.filter(item => item.supplierId === supplierId)
      setSupplierProducts(filteredProducts)
    } catch (error) {
      message.error('获取供应商产品列表失败')
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

  const handleAddProduct = () => {
    // 先获取所有产品
    fetchProducts()
    // 重置表单
    addProductForm.resetFields()
    // 打开新增关联产品对话框
    setAddProductModalVisible(true)
  }

  const handleAddProductOk = async () => {
    try {
      const values = await addProductForm.validateFields()
      // 添加供应商ID
      values.supplierId = currentSupplier.id
      // 计算库存金额和出货金额
      values.stockAmount = values.supplierPrice * values.stockQuantity
      values.salesAmount = values.supplierPrice * values.salesQuantity
      
      // 发送请求
      await request.post('/supplier-products', values)
      message.success('新增关联产品成功')
      
      // 关闭对话框
      setAddProductModalVisible(false)
      // 重新获取供应商产品列表
      await fetchSupplierProducts(currentSupplier.id)
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    }
  }

  // 充值相关函数
  const handleRechargeClick = async (supplier) => {
    setCurrentSupplier(supplier)
    // 获取供应商关联的充值记录
    await fetchSupplierRecharges(supplier.id)
    // 打开充值对话框
    setRechargeModalVisible(true)
  }

  const fetchSupplierRecharges = async (supplierId) => {
    try {
      const data = await request.get('/supplier-recharges')
      // 过滤出当前供应商的充值记录
      const filteredRecharges = data.filter(item => item.supplierId === supplierId)
      setSupplierRecharges(filteredRecharges)
    } catch (error) {
      message.error('获取供应商充值记录失败')
    }
  }

  const handleAddRecharge = () => {
    // 重置表单
    addRechargeForm.resetFields()
    // 打开新增充值对话框
    setAddRechargeModalVisible(true)
  }

  const handleAddRechargeOk = async () => {
    try {
      const values = await addRechargeForm.validateFields()
      // 添加供应商ID
      values.supplierId = currentSupplier.id
      // 获取当前用户信息
      const user = JSON.parse(localStorage.getItem('user'))
      values.operatorId = user.id
      values.operatorName = user.username
      
      // 发送请求
      await request.post('/supplier-recharges', values)
      message.success('新增充值记录成功')
      
      // 关闭对话框
      setAddRechargeModalVisible(false)
      // 重新获取供应商充值记录
      await fetchSupplierRecharges(currentSupplier.id)
      // 同时更新余额信息
      await fetchSupplierBalance(currentSupplier.id)
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    }
  }

  // 余额管理相关函数
  const handleBalanceClick = async (supplier) => {
    setCurrentSupplier(supplier)
    // 获取供应商余额信息
    await fetchSupplierBalance(supplier.id)
    // 打开余额管理对话框
    setBalanceModalVisible(true)
  }

  const fetchSupplierBalance = async (supplierId) => {
    try {
      const data = await request.get(`/supplier-balances/supplier/${supplierId}`)
      setSupplierBalance(data)
      // 设置表单值，确保类型转换正确
      balanceForm.setFieldsValue({
        totalRecharge: parseFloat(data.totalRecharge || 0),
        consumedAmount: parseFloat(data.consumedAmount || 0),
        remainingAmount: parseFloat(data.remainingAmount || 0),
        alertThreshold: data.alertThreshold ? parseFloat(data.alertThreshold) : 0
      })
    } catch (error) {
      message.error('获取供应商余额信息失败')
    }
  }

  const handleBalanceOk = async () => {
    try {
      const values = await balanceForm.validateFields()
      // 转换数值类型为字符串，以正确处理BigDecimal类型
      const formattedValues = {
        totalRecharge: values.totalRecharge.toString(),
        consumedAmount: values.consumedAmount.toString(),
        remainingAmount: values.remainingAmount.toString(),
        alertThreshold: values.alertThreshold ? values.alertThreshold.toString() : null
      }
      // 发送请求
      await request.put(`/supplier-balances/supplier/${currentSupplier.id}`, formattedValues)
      message.success('更新余额信息成功')
      
      // 关闭对话框
      setBalanceModalVisible(false)
      // 重新获取供应商余额信息
      await fetchSupplierBalance(currentSupplier.id)
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    }
  }

  const handleAdd = () => {
    setEditingSupplier(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    console.log('编辑按钮被点击，record:', record)
    if (!record) {
      message.error('编辑失败：记录不存在')
      return
    }
    
    setEditingSupplier(record)
    
    // 设置表单值
    form.setFieldsValue({
      name: record.name || '',
      supplierCode: record.supplierCode || '',
      cooperationStartDate: record.cooperationStartDate ? moment(record.cooperationStartDate) : null,
      enabled: record.enabled !== undefined ? record.enabled : true
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/suppliers/${id}`)
      message.success('删除成功')
      fetchSuppliers()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      
      if (editingSupplier) {
        // 编辑供应商
        await request.put(`/suppliers/${editingSupplier.id}`, values)
        message.success('编辑成功')
      } else {
        // 新增供应商
        await request.post('/suppliers', values)
        message.success('新增成功')
      }
      
      setModalVisible(false)
      fetchSuppliers()
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    }
  }

  const columns = [
    {
      title: '供应商名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '供应商代码',
      dataIndex: 'supplierCode',
      key: 'supplierCode',
    },
    {
      title: '合作开始时间',
      dataIndex: 'cooperationStartDate',
      key: 'cooperationStartDate',
      ellipsis: true,
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
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <Button
            type="link"
            icon={<ProductOutlined />}
            onClick={() => handleProductClick(record)}
          >
            产品
          </Button>
          <Button
            type="link"
            icon={<WalletOutlined />}
            onClick={() => handleBalanceClick(record)}
          >
            余额
          </Button>
          <Button
            type="link"
            icon={<TransactionOutlined />}
            onClick={() => handleRechargeClick(record)}
          >
            充值
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此供应商吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </div>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增供应商
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={suppliers}
        rowKey="id"
        loading={loading}
        scroll={{ x: 'max-content' }}
      />
      <Modal
        title={editingSupplier ? '编辑供应商' : '新增供应商'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="供应商名称"
            rules={[{ required: true, message: '请输入供应商名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="supplierCode"
            label="供应商代码"
          >
            <Input disabled placeholder="系统自动生成" />
          </Form.Item>
          <Form.Item
            name="cooperationStartDate"
            label="合作开始时间"
          >
            <DatePicker style={{ width: '100%' }} showTime format="YYYY-MM-DD HH:mm:ss" />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
      
      {/* 产品对话框 */}
      <Modal
        title={`${currentSupplier?.name} 的关联产品`}
        open={productModalVisible}
        onCancel={() => setProductModalVisible(false)}
        width={1000}
        footer={[
          <Button key="add" type="primary" onClick={handleAddProduct}>
            新增关联产品
          </Button>,
          <Button key="close" onClick={() => setProductModalVisible(false)}>
            关闭
          </Button>
        ]}
      >
        <Table
          columns={[
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
              title: '状态',
              dataIndex: 'enabled',
              key: 'enabled',
              render: (enabled) => (enabled ? '启用' : '禁用'),
            },
          ]}
          dataSource={supplierProducts}
          rowKey="id"
        />
      </Modal>
      
      {/* 新增关联产品对话框 */}
      <Modal
        title="新增关联产品"
        open={addProductModalVisible}
        onOk={handleAddProductOk}
        onCancel={() => setAddProductModalVisible(false)}
        width={600}
      >
        <Form form={addProductForm} layout="vertical">
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
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
      
      {/* 充值对话框 */}
      <Modal
        title={`${currentSupplier?.name} 的充值记录`}
        open={rechargeModalVisible}
        onCancel={() => setRechargeModalVisible(false)}
        width={1000}
        footer={[
          <Button key="add" type="primary" onClick={handleAddRecharge}>
            新建充值
          </Button>,
          <Button key="close" onClick={() => setRechargeModalVisible(false)}>
            关闭
          </Button>
        ]}
      >
        <Table
          columns={[
            {
              title: '充值金额',
              dataIndex: 'amount',
              key: 'amount',
            },
            {
              title: '财务截图',
              dataIndex: 'screenshotUrl',
              key: 'screenshotUrl',
              render: (url) => url ? <a href={`/api${url}`} target="_blank" rel="noopener noreferrer">查看截图</a> : '-',
            },
            {
              title: '操作人员',
              dataIndex: 'operatorName',
              key: 'operatorName',
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
          ]}
          dataSource={supplierRecharges}
          rowKey="id"
        />
      </Modal>
      
      {/* 新增充值对话框 */}
      <Modal
        title="新建充值"
        open={addRechargeModalVisible}
        onOk={handleAddRechargeOk}
        onCancel={() => setAddRechargeModalVisible(false)}
        width={600}
      >
        <Form form={addRechargeForm} layout="vertical">
          <Form.Item
            label="供应商名称"
          >
            <Input value={currentSupplier?.name} disabled />
          </Form.Item>
          <Form.Item
            name="amount"
            label="充值金额"
            rules={[{ required: true, message: '请输入充值金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="screenshotUrl"
            label="财务截图"
          >
            <Upload
              name="file"
              action="/api/upload"
              listType="picture"
              maxCount={1}
              onChange={(info) => {
                if (info.file.status === 'done') {
                  if (info.file.response && info.file.response.url) {
                    addRechargeForm.setFieldsValue({
                      screenshotUrl: info.file.response.url
                    })
                  }
                }
              }}
            >
              <Button>点击上传</Button>
            </Upload>
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
      
      {/* 余额管理对话框 */}
      <Modal
        title={`${currentSupplier?.name} 的余额管理`}
        open={balanceModalVisible}
        onOk={handleBalanceOk}
        onCancel={() => setBalanceModalVisible(false)}
        width={600}
      >
        <Form form={balanceForm} layout="vertical">
          <Form.Item
            label="供应商名称"
          >
            <Input value={currentSupplier?.name} disabled />
          </Form.Item>
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
            rules={[{ required: true, message: '请输入差值提醒金额' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={1} placeholder="输入预警金额" defaultValue={0} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default SupplierManagement
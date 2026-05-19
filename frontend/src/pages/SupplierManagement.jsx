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
  const [searchProductName, setSearchProductName] = useState('')
  const [searchStatus, setSearchStatus] = useState(null)
  const [editProductModalVisible, setEditProductModalVisible] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)
  const [editProductForm] = Form.useForm()
  
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
      // 为每个供应商获取余额信息
      const suppliersWithBalance = await Promise.all(
        data.map(async (supplier) => {
          try {
            const balance = await request.get(`/supplier-balances/supplier/${supplier.id}`)
            return {
              ...supplier,
              totalRecharge: balance.totalRecharge || 0,
              remainingAmount: balance.remainingAmount || 0
            }
          } catch (error) {
            // 如果获取余额失败，设置默认值
            return {
              ...supplier,
              totalRecharge: 0,
              remainingAmount: 0
            }
          }
        })
      )
      setSuppliers(suppliersWithBalance)
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
      let filteredProducts = data.filter(item => item.supplierId === supplierId)
      
      // 应用搜索条件
      if (searchProductName) {
        filteredProducts = filteredProducts.filter(item => 
          item.productName && item.productName.includes(searchProductName)
        )
      }
      
      if (searchStatus !== null) {
        filteredProducts = filteredProducts.filter(item => item.enabled === searchStatus)
      }
      
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

  const handleAddProduct = async () => {
    // 先获取所有产品
    await fetchProducts()
    // 先获取供应商已关联的产品列表
    await fetchSupplierProducts(currentSupplier.id)
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
      // 设置默认值
      values.stockQuantity = values.stockQuantity || 0
      values.salesQuantity = 0
      // 计算库存金额
      values.stockAmount = values.stockQuantity * (values.supplierPrice || 0)
      values.salesAmount = 0
      
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

  const handleEditProduct = (record) => {
    // 先获取所有产品，确保下拉列表有数据
    fetchProducts().then(() => {
      // 设置编辑对象
      setEditingProduct(record)
      // 填充表单
      editProductForm.setFieldsValue({
        productId: record.productId,
        supplierProductCode: record.supplierProductCode,
        supplierPrice: record.supplierPrice,
        faceValue: record.faceValue,
        stockQuantity: record.stockQuantity,
        dailyStockLimit: record.dailyStockLimit,
        enabled: record.enabled
      })
      // 打开编辑对话框
      setEditProductModalVisible(true)
    })
  }

  const handleEditProductOk = async () => {
    try {
      const values = await editProductForm.validateFields()
      // 添加供应商ID
      values.supplierId = currentSupplier.id
      // 添加缺失的字段
      values.stockQuantity = values.stockQuantity || 0
      values.salesQuantity = 0
      // 计算库存金额
      values.stockAmount = values.stockQuantity * (values.supplierPrice || 0)
      values.salesAmount = 0
      // 发送请求
      await request.put(`/supplier-products/${editingProduct.id}`, values)
      message.success('修改关联产品成功')
      
      // 关闭对话框
      setEditProductModalVisible(false)
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
      title: '充值总额',
      dataIndex: 'totalRecharge',
      key: 'totalRecharge',
      render: (_, record) => record.totalRecharge || '0',
    },
    {
      title: '供应商余额',
      dataIndex: 'remainingAmount',
      key: 'remainingAmount',
      render: (_, record) => record.remainingAmount || '0',
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
        <div style={{ marginBottom: 16, display: 'flex', gap: 16, alignItems: 'center' }}>
          <Input
            placeholder="产品名称"
            style={{ width: 200 }}
            onChange={(e) => setSearchProductName(e.target.value)}
          />
          <Select
            placeholder="状态"
            style={{ width: 120 }}
            onChange={(value) => setSearchStatus(value)}
            allowClear
          >
            <Option value={true}>启用</Option>
            <Option value={false}>禁用</Option>
          </Select>
          <Button type="primary" onClick={() => fetchSupplierProducts(currentSupplier.id)}>
            搜索
          </Button>
        </div>
        <Table
          columns={[
            {
              title: '产品名称',
              dataIndex: 'productName',
              key: 'productName',
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
              title: '供应商产品码',
              dataIndex: 'supplierProductCode',
              key: 'supplierProductCode',
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
              title: '库存金额',
              dataIndex: 'stockAmount',
              key: 'stockAmount',
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
                <Button
                  type="link"
                  icon={<EditOutlined />}
                  onClick={() => handleEditProduct(record)}
                >
                  修改
                </Button>
              ),
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
            <Select 
              style={{ width: '100%' }}
              showSearch
              filterOption={(input, option) => {
                // 提取option中的所有文本内容
                const getText = (node) => {
                  if (typeof node === 'string') {
                    return node
                  } else if (Array.isArray(node)) {
                    return node.map(getText).join('')
                  } else if (node && typeof node === 'object' && node.props) {
                    return getText(node.props.children)
                  }
                  return ''
                }
                const optionText = getText(option.children)
                return optionText.toLowerCase().includes(input.toLowerCase())
              }}
            >
              {products.filter(product => {
                // 过滤掉已关联的产品
                const isAssociated = supplierProducts.some(sp => sp.productId === product.id)
                return !isAssociated
              }).map(product => (
                <Option key={product.id} value={product.id}>
                  {product.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="supplierProductCode"
            label="供应商产品码"
            rules={[{ required: true, message: '请输入供应商产品码' }]}
          >
            <Input style={{ width: '100%' }} placeholder="请输入供应商产品码" />
          </Form.Item>
          <Form.Item
            name="faceValue"
            label="面值"
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} placeholder="请输入面值" />
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
          >
            <InputNumber style={{ width: '100%' }} min={0} placeholder="请输入库存量" />
          </Form.Item>
          <Form.Item
            name="dailyStockLimit"
            label="每日库存限量"
            rules={[{ required: true, message: '请输入每日库存限量' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked" initialValue={true}>
            <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
          </Form.Item>
        </Form>
      </Modal>
      
      {/* 编辑关联产品对话框 */}
      <Modal
        title="修改关联产品"
        open={editProductModalVisible}
        onOk={handleEditProductOk}
        onCancel={() => setEditProductModalVisible(false)}
        width={600}
      >
        <Form form={editProductForm} layout="vertical">
          <Form.Item
            name="productId"
            label="产品"
          >
            <Select style={{ width: '100%' }} disabled>
              {products.map(product => (
                <Option key={product.id} value={product.id}>
                  {product.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="supplierProductCode"
            label="供应商产品码"
          >
            <Input style={{ width: '100%' }} disabled />
          </Form.Item>
          <Form.Item
            name="supplierPrice"
            label="供应商价格"
            rules={[{ required: true, message: '请输入供应商价格' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="faceValue"
            label="面值"
          >
            <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
          </Form.Item>
          <Form.Item
            name="stockQuantity"
            label="库存量"
          >
            <InputNumber style={{ width: '100%' }} min={0} placeholder="请输入库存量" />
          </Form.Item>
          <Form.Item
            name="dailyStockLimit"
            label="每日库存限量"
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
            <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
              支持上传图片文件，单个文件大小不超过2MB
            </div>
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
          >
            <InputNumber 
              style={{ width: '100%' }} 
              min={0} 
              step={0.01} 
              disabled 
              placeholder="系统自动计算"
            />
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
          >
            <InputNumber 
              style={{ width: '100%' }} 
              min={0} 
              step={0.01} 
              disabled 
              placeholder="系统自动计算"
            />
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
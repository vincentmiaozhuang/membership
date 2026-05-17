import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm, DatePicker, Upload } from 'antd'
import { PlusOutlined, DeleteOutlined, SearchOutlined, ExportOutlined, ImportOutlined } from '@ant-design/icons'
import request from '../utils/request'
import dayjs from 'dayjs'
import * as XLSX from 'xlsx'

const RechargeRecordManagement = () => {
  const [records, setRecords] = useState([])
  const [products, setProducts] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [relatedSuppliers, setRelatedSuppliers] = useState([])
  const [customers, setCustomers] = useState([])
  const [allCustomers, setAllCustomers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [importModalVisible, setImportModalVisible] = useState(false)
  const [importForm] = Form.useForm()
  const [importedPhones, setImportedPhones] = useState([])
  const [searchRechargePhone, setSearchRechargePhone] = useState('')
  const [searchStatus, setSearchStatus] = useState('')
  const [searchProductId, setSearchProductId] = useState('')
  const [searchCustomerId, setSearchCustomerId] = useState('')
  const [searchSupplierId, setSearchSupplierId] = useState('')
  const [searchStartDate, setSearchStartDate] = useState(null)
  const [searchEndDate, setSearchEndDate] = useState(null)
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

  const handleImport = () => {
    importForm.resetFields()
    setImportedPhones([])
    setImportModalVisible(true)
  }

  const handleFileUpload = (info) => {
    const { file } = info
    
    const rawFile = file.originFileObj || file
    
    if (rawFile) {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const data = new Uint8Array(e.target.result)
          const workbook = XLSX.read(data, { type: 'array' })
          const firstSheetName = workbook.SheetNames[0]
          const worksheet = workbook.Sheets[firstSheetName]
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
          
          console.log('Excel原始数据:', jsonData)
          
          // 提取所有单元格中的内容
          let phones = []
          
          // 检查第一行是否是表头（包含"手机号"等文字）
          let startIndex = 0
          if (jsonData.length > 0 && jsonData[0].length > 0) {
            const firstCell = String(jsonData[0][0])
            // 如果第一行包含"手机号"、"phone"等关键字，则跳过
            if (firstCell.includes('手机号') || firstCell.toLowerCase().includes('phone')) {
              startIndex = 1
            }
          }
          
          jsonData.slice(startIndex).forEach(row => {
            row.forEach(cell => {
              if (cell) {
                // 如果单元格内容包含逗号或其他分隔符，则拆分
                const cellStr = String(cell)
                if (cellStr.includes(',')) {
                  phones.push(...cellStr.split(',').map(p => p.trim()).filter(p => p))
                } else if (cellStr.includes('，')) {
                  phones.push(...cellStr.split('，').map(p => p.trim()).filter(p => p))
                } else if (cellStr.includes('\n')) {
                  phones.push(...cellStr.split('\n').map(p => p.trim()).filter(p => p))
                } else if (cellStr.includes('\r\n')) {
                  phones.push(...cellStr.split('\r\n').map(p => p.trim()).filter(p => p))
                } else if (cellStr.includes('\t')) {
                  phones.push(...cellStr.split('\t').map(p => p.trim()).filter(p => p))
                } else if (cellStr.includes(' ')) {
                  phones.push(...cellStr.split(' ').map(p => p.trim()).filter(p => p))
                } else {
                  phones.push(cellStr)
                }
              }
            })
          })
          
          console.log('提取的所有手机号:', phones)
          console.log('提取的手机号数量:', phones.length)
          
          // 验证手机号格式
          const validPhones = []
          const invalidPhones = []
          
          phones.forEach(phone => {
            const phoneStr = String(phone).trim()
            console.log(`验证手机号: "${phoneStr}", 长度: ${phoneStr.length}, 是否匹配: ${/^1[3-9]\d{9}$/.test(phoneStr)}`)
            if (/^1[3-9]\d{9}$/.test(phoneStr)) {
              validPhones.push(phoneStr)
            } else if (phoneStr) {
              invalidPhones.push(phoneStr)
            }
          })
          
          console.log('有效手机号:', validPhones)
          console.log('无效手机号:', invalidPhones)
          
          if (invalidPhones.length > 0) {
            message.warning(`以下手机号格式不正确：${invalidPhones.join(', ')}`)
          }
          
          if (validPhones.length > 0) {
            setImportedPhones(validPhones)
            message.success(`成功导入 ${validPhones.length} 个有效手机号`)
          } else {
            message.error('没有有效的手机号')
          }
        } catch (error) {
          console.error('Parse error:', error)
          message.error('文件解析失败')
        }
      }
      reader.readAsArrayBuffer(rawFile)
    } else {
      console.error('No file object found')
    }
    return false // 阻止自动上传
  }

  const handleImportOk = async () => {
    try {
      const values = await importForm.validateFields()
      
      if (importedPhones.length === 0) {
        message.warning('请先导入手机号')
        return
      }

      // 批量创建充值记录
      const promises = importedPhones.map(phone => {
        const record = {
          rechargePhone: phone,
          productId: values.productId,
          productFacePrice: values.productFacePrice,
          productName: values.productName,
          customerId: values.customerId,
          customerPrice: values.customerPrice,
          customerName: values.customerName,
          supplierId: values.supplierId,
          supplierPrice: values.supplierPrice,
          supplierName: values.supplierName,
          status: values.status,
          rechargePerson: '0',
          description: '批量导入'
        }
        return request.post('/recharge-records', record)
      })

      await Promise.all(promises)
      message.success(`成功创建 ${importedPhones.length} 条充值记录`)
      setImportModalVisible(false)
      fetchRecords()
    } catch (error) {
      console.error(error)
    }
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
      
      // 按开始日期查询
      if (searchStartDate) {
        const startDate = searchStartDate.startOf('day')
        data = data.filter(record => {
          const recordDate = dayjs(record.createdAt)
          return recordDate.isAfter(startDate) || recordDate.isSame(startDate)
        })
      }
      
      // 按结束日期查询
      if (searchEndDate) {
        const endDate = searchEndDate.endOf('day')
        data = data.filter(record => {
          const recordDate = dayjs(record.createdAt)
          return recordDate.isBefore(endDate) || recordDate.isSame(endDate)
        })
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
    setSearchStartDate(null)
    setSearchEndDate(null)
    fetchRecords()
  }

  const handleExport = () => {
    if (records.length === 0) {
      message.warning('没有可导出的数据')
      return
    }

    // 准备导出数据，按照页面列的顺序
    const exportData = records.map(record => ({
      '充值手机号': record.rechargePhone || '',
      '产品名称': record.productName || '',
      '平台订单ID': record.platformOrderId || '',
      '客户订单ID': record.customerOrderId || '',
      '产品票面价格': record.productFacePrice || '',
      '客户名称': record.customerName || '',
      '客户价格': record.customerPrice || '',
      '供应商名称': record.supplierName || '',
      '供应商价格': record.supplierPrice || '',
      '状态': record.status || '',
      '描述': record.description || '',
      '充值人': record.rechargePerson || '',
      '充值时间': record.createdAt ? dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss') : '',
    }))

    // 创建工作簿和工作表
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '充值记录')

    // 设置列宽
    ws['!cols'] = [
      { wch: 15 }, // 充值手机号
      { wch: 20 }, // 产品名称
      { wch: 20 }, // 平台订单ID
      { wch: 20 }, // 客户订单ID
      { wch: 15 }, // 产品票面价格
      { wch: 15 }, // 客户名称
      { wch: 12 }, // 客户价格
      { wch: 15 }, // 供应商名称
      { wch: 12 }, // 供应商价格
      { wch: 10 }, // 状态
      { wch: 30 }, // 描述
      { wch: 10 }, // 充值人
      { wch: 20 }, // 充值时间
    ]

    // 设置所有单元格上下居中对齐和左对齐
    const range = XLSX.utils.decode_range(ws['!ref'])
    for (let R = range.s.r; R <= range.e.r; ++R) {
      for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: R, c: C })
        if (!ws[cellAddress]) continue
        if (!ws[cellAddress].s) ws[cellAddress].s = {}
        ws[cellAddress].s.alignment = { horizontal: 'left', vertical: 'center' }
      }
    }

    // 生成文件名
    const fileName = `充值记录_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`

    // 导出文件
    XLSX.writeFile(wb, fileName)
    message.success('导出成功')
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
        
        <Button type="primary" icon={<ImportOutlined />} onClick={handleImport}>
          批量充值
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
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>开始日期：</span>
          <DatePicker
            placeholder="请选择开始日期"
            value={searchStartDate}
            onChange={setSearchStartDate}
            style={{ width: 160, borderRadius: 4 }}
            allowClear
          />
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ whiteSpace: 'nowrap', fontWeight: 500, color: '#333' }}>结束日期：</span>
          <DatePicker
            placeholder="请选择结束日期"
            value={searchEndDate}
            onChange={setSearchEndDate}
            style={{ width: 160, borderRadius: 4 }}
            allowClear
          />
        </div>
        
        <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} style={{ marginLeft: 8 }}>
          查询
        </Button>
        
        <Button onClick={handleReset} style={{ marginLeft: 8 }}>
          重置
        </Button>
        
        <Button type="primary" icon={<ExportOutlined />} onClick={handleExport} style={{ marginLeft: 8 }}>
          导出
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
      
      <Modal
        title="导入充值手机号"
        open={importModalVisible}
        onOk={handleImportOk}
        onCancel={() => setImportModalVisible(false)}
        width={600}
      >
        <Form form={importForm} layout="vertical">
          <Form.Item label="上传Excel文件">
            <Upload
              beforeUpload={() => false}
              onChange={handleFileUpload}
              accept=".xlsx,.xls"
              maxCount={1}
            >
              <Button icon={<ImportOutlined />}>选择Excel文件</Button>
            </Upload>
            <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
              请上传包含手机号的Excel文件，第一列为手机号
            </div>
          </Form.Item>
          
          {importedPhones.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 8, fontWeight: 500 }}>
                已导入 {importedPhones.length} 个有效手机号：
              </div>
              <div style={{ 
                maxHeight: 150, 
                overflowY: 'auto', 
                border: '1px solid #d9d9d9', 
                borderRadius: 4, 
                padding: 8 
              }}>
                {importedPhones.map((phone, index) => (
                  <div key={index} style={{ padding: '4px 0' }}>{phone}</div>
                ))}
              </div>
            </div>
          )}
          
          <Form.Item
            name="productName"
            label="产品名称"
            rules={[{ required: true, message: '请选择产品名称' }]}
            style={{ marginTop: 16 }}
          >
            <Select 
              placeholder="请选择产品名称"
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
              onChange={(value) => {
                const selectedProduct = products.find(p => p.name === value)
                if (selectedProduct) {
                  importForm.setFieldsValue({
                    productId: selectedProduct.id,
                    productFacePrice: selectedProduct.faceValue,
                    productName: selectedProduct.name
                  })
                  fetchCustomersByProductId(selectedProduct.id)
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
                const selectedCustomer = customers.find(c => c.name === value)
                if (selectedCustomer) {
                  importForm.setFieldsValue({
                    customerId: selectedCustomer.id,
                    customerPrice: selectedCustomer.price,
                    customerName: selectedCustomer.name
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
                const selectedSupplier = relatedSuppliers.find(s => s.name === value)
                if (selectedSupplier) {
                  importForm.setFieldsValue({
                    supplierId: selectedSupplier.id,
                    supplierPrice: selectedSupplier.price,
                    supplierName: selectedSupplier.name
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
            initialValue="充值中"
          >
            <Select placeholder="请选择充值状态">
              <Select.Option value="成功">成功</Select.Option>
              <Select.Option value="失败">失败</Select.Option>
              <Select.Option value="充值中">充值中</Select.Option>
            </Select>
          </Form.Item>
          
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
          <Form.Item name="supplierId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="supplierPrice" hidden>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RechargeRecordManagement

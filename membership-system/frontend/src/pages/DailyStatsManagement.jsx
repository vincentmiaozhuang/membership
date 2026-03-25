import React, { useState, useEffect } from 'react'
import { Table, Button, message, DatePicker, Select, Space, Row, Col, Input } from 'antd'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import request from '../utils/request'

const { RangePicker } = DatePicker
const { Option } = Select

const DailyStatsManagement = () => {
  const [statsList, setStatsList] = useState([])
  const [loading, setLoading] = useState(false)
  const [pageSize, setPageSize] = useState(50)
  const [currentPage, setCurrentPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [searchParams, setSearchParams] = useState({
    dateType: 'all',
    startDate: null,
    endDate: null,
    productId: null,
    productName: null,
    customerId: null,
    customerName: null,
    supplierId: null,
    supplierName: null,
    status: null
  })
  const [products, setProducts] = useState([])
  const [customers, setCustomers] = useState([])
  const [suppliers, setSuppliers] = useState([])

  useEffect(() => {
    fetchStatsList()
    fetchSelectOptions()
  }, [])

  const fetchSelectOptions = async () => {
    try {
      // 从API获取产品列表
      const productsResponse = await request.get('/products')
      setProducts(productsResponse)
      
      // 从API获取客户列表
      const customersResponse = await request.get('/customers')
      setCustomers(customersResponse)
      
      // 从API获取供应商列表
      const suppliersResponse = await request.get('/suppliers')
      setSuppliers(suppliersResponse)
    } catch (error) {
      console.error('获取选项失败:', error)
      // 使用模拟数据作为备份
      setProducts([
        { id: 5, name: '腾讯视频月会员' },
        { id: 6, name: '爱奇艺月会员' }
      ])
      setCustomers([
        { id: 7, name: '中国铁建' },
        { id: 8, name: '中国烟草' },
        { id: 9, name: '中国建设' }
      ])
      setSuppliers([
        { id: 5, name: '爱奇艺' },
        { id: 6, name: '腾讯视频' }
      ])
    }
  }

  const fetchStatsList = async () => {
    setLoading(true)
    try {
      console.log('开始获取每日充值汇总数据...')
      console.log('当前搜索参数:', searchParams)
      // 构建查询参数
      const params = {}
      
      // 日期类型
      if (searchParams.dateType && searchParams.dateType !== 'all') {
        params.dateType = searchParams.dateType
        console.log('添加日期类型参数:', searchParams.dateType)
      }
      
      // 产品ID
      if (searchParams.productId !== null && searchParams.productId !== undefined) {
        params.productId = searchParams.productId
        console.log('添加产品ID参数:', searchParams.productId)
      } else {
        console.log('产品ID为null，不添加该参数')
      }
      
      // 客户ID
      if (searchParams.customerId !== null && searchParams.customerId !== undefined) {
        params.customerId = searchParams.customerId
        console.log('添加客户ID参数:', searchParams.customerId)
      } else {
        console.log('客户ID为null，不添加该参数')
      }
      
      // 供应商ID
      if (searchParams.supplierId !== null && searchParams.supplierId !== undefined) {
        params.supplierId = searchParams.supplierId
        console.log('添加供应商ID参数:', searchParams.supplierId)
      } else {
        console.log('供应商ID为null，不添加该参数')
      }
      
      // 状态
      if (searchParams.status !== null && searchParams.status !== undefined) {
        params.status = searchParams.status
        console.log('添加状态参数:', searchParams.status)
      } else {
        console.log('状态为null，不添加该参数')
      }
      
      // 添加日期参数
      if (searchParams.dateType === 'range') {
        if (searchParams.startDate) {
          params.startDate = searchParams.startDate.format('YYYY-MM-DD')
          console.log('添加开始日期参数:', params.startDate)
        }
        if (searchParams.endDate) {
          params.endDate = searchParams.endDate.format('YYYY-MM-DD')
          console.log('添加结束日期参数:', params.endDate)
        }
      }
      
      console.log('最终查询参数:', params)
      console.log('发起请求到:', '/stats/daily')
      const response = await request.get('/stats/daily', { params })
      console.log('获取每日充值汇总数据成功:', response)
      console.log('响应类型:', typeof response)
      console.log('响应长度:', response ? response.length : 0)
      
      // 后端返回的是直接的列表数据
      setStatsList(response)
      setTotal(response.length)
      console.log('设置每日充值汇总数据:', response)
    } catch (error) {
      console.error('获取每日充值汇总数据失败:', error)
      console.error('错误详情:', error.response)
      console.error('错误状态:', error.response ? error.response.status : '无响应')
      console.error('错误数据:', error.response ? error.response.data : '无数据')
      message.error('获取每日充值汇总数据失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    // 这里应该根据搜索参数过滤数据
    // 暂时只刷新数据
    fetchStatsList()
  }

  const handleReset = () => {
    setSearchParams({
      dateType: 'all',
      startDate: null,
      endDate: null,
      productId: null,
      productName: null,
      customerId: null,
      customerName: null,
      supplierId: null,
      supplierName: null,
      status: null
    })
    fetchStatsList()
  }

  const columns = [
    {
      title: '产品',
      dataIndex: 'productName',
      key: 'productName',
      width: 120,
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 120,
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      key: 'supplierName',
      width: 120,
    },
    {
      title: '日期',
      dataIndex: 'statDate',
      key: 'statDate',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => {
        const color = status === '成功' ? 'green' : status === '失败' ? 'red' : 'orange'
        return <span style={{ color }}>{status}</span>
      },
    },
    {
      title: '产品面值',
      dataIndex: 'productFacePrice',
      key: 'productFacePrice',
      width: 100,
    },
    {
      title: '成本单价',
      dataIndex: 'supplierPrice',
      key: 'supplierPrice',
      width: 100,
    },
    {
      title: '售卖单价',
      dataIndex: 'customerPrice',
      key: 'customerPrice',
      width: 100,
    },
    {
      title: '数量',
      dataIndex: 'rechargeCount',
      key: 'rechargeCount',
      width: 80,
    },
    {
      title: '成本金额',
      dataIndex: 'costAmount',
      key: 'costAmount',
      width: 100,
    },
    {
      title: '销售金额',
      dataIndex: 'customerAmount',
      key: 'customerAmount',
      width: 100,
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
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.09)'
      }}>
        {/* 第一行：日期选项 */}
        <Row gutter={12} align="middle" style={{ marginBottom: 12 }}>
          <Col span={1}>
            <label style={{ marginBottom: 0 }}>日期范围</label>
          </Col>
          <Col span={2}>
            <Select 
              style={{ width: '100%' }} 
              value={searchParams.dateType}
              onChange={(value) => setSearchParams({ ...searchParams, dateType: value })}
            >
              <Option value="all">全部</Option>
              <Option value="today">当日</Option>
              <Option value="range">日期段</Option>
            </Select>
          </Col>
          {searchParams.dateType === 'range' && (
            <>
              <Col span={1}>
                <label style={{ marginBottom: 0 }}>开始日期</label>
              </Col>
              <Col span={2}>
                <DatePicker 
                  style={{ width: '100%' }} 
                  onChange={(date) => {
                    if (date) {
                      setSearchParams({
                        ...searchParams,
                        startDate: date
                      })
                    }
                  }}
                />
              </Col>
              <Col span={1}>
                <label style={{ marginBottom: 0 }}>结束日期</label>
              </Col>
              <Col span={2}>
                <DatePicker 
                  style={{ width: '100%' }} 
                  onChange={(date) => {
                    if (date) {
                      setSearchParams({
                        ...searchParams,
                        endDate: date
                      })
                    }
                  }}
                />
              </Col>
            </>
          )}
        </Row>
        
        {/* 第二行：产品、客户、供应商、状态、按钮 */}
        <Row gutter={12} align="middle">
          <Col span={1}>
            <label style={{ marginBottom: 0 }}>产品</label>
          </Col>
          <Col span={2}>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择产品"
              value={searchParams.productId ? { value: searchParams.productId, label: searchParams.productName } : { value: null, label: '全部' }}
              onChange={(value) => {
                console.log('产品选择变化:', value);
                setSearchParams({ 
                  ...searchParams, 
                  productId: value.value !== null ? Number(value.value) : null, 
                  productName: value.value !== null ? value.label : null 
                });
              }}
              labelInValue
            >
              <Option value={null}>全部</Option>
              {products.map(product => (
                <Option key={product.id} value={String(product.id)}>{product.name}</Option>
              ))}
            </Select>
          </Col>
          <Col span={1}>
            <label style={{ marginBottom: 0 }}>客户</label>
          </Col>
          <Col span={2}>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择客户"
              value={searchParams.customerId ? { value: searchParams.customerId, label: searchParams.customerName } : { value: null, label: '全部' }}
              onChange={(value) => {
                console.log('客户选择变化:', value);
                setSearchParams({ 
                  ...searchParams, 
                  customerId: value.value !== null ? Number(value.value) : null, 
                  customerName: value.value !== null ? value.label : null 
                });
              }}
              labelInValue
            >
              <Option value={null}>全部</Option>
              {customers.map(customer => (
                <Option key={customer.id} value={String(customer.id)}>{customer.name}</Option>
              ))}
            </Select>
          </Col>
          <Col span={1}>
            <label style={{ marginBottom: 0 }}>供应商</label>
          </Col>
          <Col span={2}>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择供应商"
              value={searchParams.supplierId ? { value: searchParams.supplierId, label: searchParams.supplierName } : { value: null, label: '全部' }}
              onChange={(value) => {
                console.log('供应商选择变化:', value);
                setSearchParams({ 
                  ...searchParams, 
                  supplierId: value.value !== null ? Number(value.value) : null, 
                  supplierName: value.value !== null ? value.label : null 
                });
              }}
              labelInValue
            >
              <Option value={null}>全部</Option>
              {suppliers.map(supplier => (
                <Option key={supplier.id} value={String(supplier.id)}>{supplier.name}</Option>
              ))}
            </Select>
          </Col>
          <Col span={1}>
            <label style={{ marginBottom: 0 }}>状态</label>
          </Col>
          <Col span={2}>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择状态"
              value={searchParams.status !== null ? searchParams.status : '全部'}
              onChange={(value) => {
                console.log('状态选择变化:', value);
                setSearchParams({ ...searchParams, status: value !== '全部' ? value : null });
              }}
            >
              <Option value="全部">全部</Option>
              <Option value="成功">成功</Option>
              <Option value="失败">失败</Option>
              <Option value="充值中">充值中</Option>
            </Select>
          </Col>
          <Col span={4} style={{ textAlign: 'right' }}>
            <Space>
              <Button 
                type="primary" 
                icon={<SearchOutlined />} 
                onClick={handleSearch}
              >
                搜索
              </Button>
              <Button onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </div>
      <div style={{ overflowX: 'auto' }}>
        <Table
          columns={columns}
          dataSource={statsList}
          rowKey="id"
          loading={loading}
          scroll={{ x: 'max-content' }}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
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
    </div>
  )
}

export default DailyStatsManagement
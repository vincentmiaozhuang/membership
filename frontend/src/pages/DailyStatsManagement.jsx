import React, { useState, useEffect } from 'react'
import { Table, Button, message, DatePicker, Select, Space, Row, Col, Card, Statistic, Divider } from 'antd'
import { ReloadOutlined, SearchOutlined, FilterOutlined, DownloadOutlined, CalendarOutlined, ShoppingOutlined, UserOutlined, BankOutlined, CheckCircleOutlined } from '@ant-design/icons'
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

  // 计算统计数据
  const totalCount = statsList.reduce((sum, item) => sum + (item.rechargeCount || 0), 0)
  const totalCostAmount = statsList.reduce((sum, item) => sum + (parseFloat(item.costAmount) || 0), 0)
  const totalSalesAmount = statsList.reduce((sum, item) => sum + (parseFloat(item.customerAmount) || 0), 0)
  const profit = totalSalesAmount - totalCostAmount

  const columns = [
    {
      title: '产品',
      dataIndex: 'productName',
      key: 'productName',
      width: 120,
      render: (text) => (
        <span style={{ fontWeight: 500, color: '#3f4254' }}>{text}</span>
      ),
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 120,
      render: (text) => (
        <span style={{ color: '#5e6278' }}>{text}</span>
      ),
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      key: 'supplierName',
      width: 120,
      render: (text) => (
        <span style={{ color: '#5e6278' }}>{text}</span>
      ),
    },
    {
      title: '日期',
      dataIndex: 'statDate',
      key: 'statDate',
      width: 100,
      render: (text) => (
        <span style={{ fontWeight: 500, color: '#3f4254' }}>{text}</span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => {
        const statusConfig = {
          '成功': { color: '#50cd89', bgColor: '#e8fff3', label: '成功' },
          '失败': { color: '#f1416c', bgColor: '#fff5f8', label: '失败' },
          '充值中': { color: '#ffc700', bgColor: '#fff8dd', label: '充值中' }
        }
        const config = statusConfig[status] || { color: '#7e8299', bgColor: '#f5f8fa', label: status }
        return (
          <span style={{ 
            color: config.color, 
            backgroundColor: config.bgColor,
            padding: '4px 12px',
            borderRadius: '6px',
            fontSize: '12px',
            fontWeight: 600,
            display: 'inline-block'
          }}>
            {config.label}
          </span>
        )
      },
    },
    {
      title: '产品面值',
      dataIndex: 'productFacePrice',
      key: 'productFacePrice',
      width: 100,
      align: 'right',
      render: (text) => (
        <span style={{ fontWeight: 500, color: '#3f4254' }}>¥{text}</span>
      ),
    },
    {
      title: '成本单价',
      dataIndex: 'supplierPrice',
      key: 'supplierPrice',
      width: 100,
      align: 'right',
      render: (text) => (
        <span style={{ color: '#5e6278' }}>¥{text}</span>
      ),
    },
    {
      title: '售卖单价',
      dataIndex: 'customerPrice',
      key: 'customerPrice',
      width: 100,
      align: 'right',
      render: (text) => (
        <span style={{ color: '#5e6278' }}>¥{text}</span>
      ),
    },
    {
      title: '数量',
      dataIndex: 'rechargeCount',
      key: 'rechargeCount',
      width: 80,
      align: 'center',
      render: (text) => (
        <span style={{ fontWeight: 600, color: '#3f4254' }}>{text}</span>
      ),
    },
    {
      title: '成本金额',
      dataIndex: 'costAmount',
      key: 'costAmount',
      width: 120,
      align: 'right',
      render: (text) => (
        <span style={{ fontWeight: 600, color: '#f1416c' }}>¥{text}</span>
      ),
    },
    {
      title: '销售金额',
      dataIndex: 'customerAmount',
      key: 'customerAmount',
      width: 120,
      align: 'right',
      render: (text) => (
        <span style={{ fontWeight: 600, color: '#50cd89' }}>¥{text}</span>
      ),
    },
  ]

  return (
    <div style={{ padding: '24px', backgroundColor: '#f3f6f9', minHeight: '100vh' }}>
      {/* 页面标题 */}
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ 
          fontSize: '24px', 
          fontWeight: 600, 
          color: '#181c32',
          margin: 0,
          marginBottom: '8px'
        }}>
          每日充值汇总
        </h1>
        <p style={{ 
          fontSize: '14px', 
          color: '#7e8299',
          margin: 0
        }}>
          查看每日充值数据统计和汇总信息
        </p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[24, 24]} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} md={6}>
          <Card 
            bordered={false}
            style={{ 
              borderRadius: '12px',
              boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px' }}>总充值数量</span>}
              value={totalCount}
              prefix={<ShoppingOutlined style={{ color: '#fff' }} />}
              valueStyle={{ color: '#fff', fontSize: '28px', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card 
            bordered={false}
            style={{ 
              borderRadius: '12px',
              boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)',
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px' }}>总成本金额</span>}
              value={totalCostAmount.toFixed(2)}
              prefix={<BankOutlined style={{ color: '#fff' }} />}
              valueStyle={{ color: '#fff', fontSize: '28px', fontWeight: 700 }}
              suffix="¥"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card 
            bordered={false}
            style={{ 
              borderRadius: '12px',
              boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)',
              background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px' }}>总销售金额</span>}
              value={totalSalesAmount.toFixed(2)}
              prefix={<UserOutlined style={{ color: '#fff' }} />}
              valueStyle={{ color: '#fff', fontSize: '28px', fontWeight: 700 }}
              suffix="¥"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card 
            bordered={false}
            style={{ 
              borderRadius: '12px',
              boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)',
              background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px' }}>总利润</span>}
              value={profit.toFixed(2)}
              prefix={<CheckCircleOutlined style={{ color: '#fff' }} />}
              valueStyle={{ color: '#fff', fontSize: '28px', fontWeight: 700 }}
              suffix="¥"
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索筛选卡片 */}
      <Card 
        bordered={false}
        style={{ 
          marginBottom: '24px',
          borderRadius: '12px',
          boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)'
        }}
        title={
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <FilterOutlined style={{ color: '#009ef7', marginRight: '8px', fontSize: '18px' }} />
            <span style={{ fontSize: '16px', fontWeight: 600, color: '#181c32' }}>筛选条件</span>
          </div>
        }
      >
        <Row gutter={[24, 16]} align="middle">
          {/* 日期范围 */}
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <label style={{ 
                fontSize: '13px', 
                fontWeight: 600, 
                color: '#3f4254',
                display: 'flex',
                alignItems: 'center'
              }}>
                <CalendarOutlined style={{ marginRight: '6px', color: '#009ef7' }} />
                日期范围
              </label>
            </div>
            <Select 
              style={{ width: '100%' }} 
              value={searchParams.dateType}
              onChange={(value) => setSearchParams({ ...searchParams, dateType: value })}
              size="large"
            >
              <Option value="all">全部</Option>
              <Option value="today">当日</Option>
              <Option value="range">日期段</Option>
            </Select>
          </Col>
          
          {searchParams.dateType === 'range' && (
            <>
              <Col xs={24} sm={12} md={8} lg={6}>
                <div style={{ marginBottom: '8px' }}>
                  <label style={{ 
                    fontSize: '13px', 
                    fontWeight: 600, 
                    color: '#3f4254'
                  }}>
                    开始日期
                  </label>
                </div>
                <DatePicker 
                  style={{ width: '100%' }} 
                  size="large"
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
              <Col xs={24} sm={12} md={8} lg={6}>
                <div style={{ marginBottom: '8px' }}>
                  <label style={{ 
                    fontSize: '13px', 
                    fontWeight: 600, 
                    color: '#3f4254'
                  }}>
                    结束日期
                  </label>
                </div>
                <DatePicker 
                  style={{ width: '100%' }} 
                  size="large"
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
          
          {/* 产品 */}
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <label style={{ 
                fontSize: '13px', 
                fontWeight: 600, 
                color: '#3f4254',
                display: 'flex',
                alignItems: 'center'
              }}>
                <ShoppingOutlined style={{ marginRight: '6px', color: '#009ef7' }} />
                产品
              </label>
            </div>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择产品"
              size="large"
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
          
          {/* 客户 */}
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <label style={{ 
                fontSize: '13px', 
                fontWeight: 600, 
                color: '#3f4254',
                display: 'flex',
                alignItems: 'center'
              }}>
                <UserOutlined style={{ marginRight: '6px', color: '#009ef7' }} />
                客户
              </label>
            </div>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择客户"
              size="large"
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
          
          {/* 供应商 */}
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <label style={{ 
                fontSize: '13px', 
                fontWeight: 600, 
                color: '#3f4254',
                display: 'flex',
                alignItems: 'center'
              }}>
                <BankOutlined style={{ marginRight: '6px', color: '#009ef7' }} />
                供应商
              </label>
            </div>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择供应商"
              size="large"
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
          
          {/* 状态 */}
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <label style={{ 
                fontSize: '13px', 
                fontWeight: 600, 
                color: '#3f4254',
                display: 'flex',
                alignItems: 'center'
              }}>
                <CheckCircleOutlined style={{ marginRight: '6px', color: '#009ef7' }} />
                状态
              </label>
            </div>
            <Select 
              style={{ width: '100%' }} 
              placeholder="选择状态"
              size="large"
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
          
          {/* 操作按钮 */}
          <Col xs={24} sm={12} md={8} lg={6} style={{ display: 'flex', alignItems: 'flex-end' }}>
            <Space size="middle">
              <Button 
                type="primary" 
                icon={<SearchOutlined />} 
                onClick={handleSearch}
                size="large"
                style={{ 
                  background: 'linear-gradient(135deg, #009ef7 0%, #0056b3 100%)',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: 600,
                  boxShadow: '0 4px 12px rgba(0, 158, 247, 0.3)'
                }}
              >
                搜索
              </Button>
              <Button 
                onClick={handleReset}
                size="large"
                style={{ 
                  borderRadius: '8px',
                  fontWeight: 600
                }}
              >
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 数据表格卡片 */}
      <Card 
        bordered={false}
        style={{ 
          borderRadius: '12px',
          boxShadow: '0 0 20px rgba(76, 87, 125, 0.05)'
        }}
        title={
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ 
                width: '4px', 
                height: '20px', 
                background: 'linear-gradient(135deg, #009ef7 0%, #0056b3 100%)',
                borderRadius: '2px',
                marginRight: '12px'
              }} />
              <span style={{ fontSize: '16px', fontWeight: 600, color: '#181c32' }}>数据列表</span>
            </div>
            <Button 
              icon={<DownloadOutlined />}
              style={{ 
                borderRadius: '8px',
                fontWeight: 500
              }}
            >
              导出数据
            </Button>
          </div>
        }
      >
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
            defaultPageSize: 50,
            style: { marginTop: '16px' }
          }}
          style={{
            '.ant-table-thead > tr > th': {
              backgroundColor: '#f5f8fa',
              color: '#3f4254',
              fontWeight: 600,
              fontSize: '13px',
              borderBottom: '1px solid #e4e6ef'
            },
            '.ant-table-tbody > tr > td': {
              borderBottom: '1px solid #e4e6ef',
              fontSize: '13px'
            },
            '.ant-table-tbody > tr:hover > td': {
              backgroundColor: '#f8f9fa'
            }
          }}
        />
      </Card>
    </div>
  )
}

export default DailyStatsManagement
const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

app.use(cors({
  origin: ['http://localhost:5173', 'http://localhost:5174', 'http://localhost:5175', 'http://localhost:5176', 'http://localhost:5177', 'http://localhost:5178'],
  credentials: true
}));
app.use(express.json());

// 模拟数据
const mockSuppliers = [
  {
    id: 1,
    name: '中国移动',
    supplierCode: 'CMCC001',
    cooperationStartDate: '2024-01-01T00:00:00',
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 2,
    name: '中国联通',
    supplierCode: 'CUCC001',
    cooperationStartDate: '2024-02-01T00:00:00',
    enabled: true,
    createdAt: '2024-02-01T00:00:00',
    updatedAt: '2024-02-01T00:00:00'
  },
  {
    id: 3,
    name: '中国电信',
    supplierCode: 'CTCC001',
    cooperationStartDate: '2024-03-01T00:00:00',
    enabled: true,
    createdAt: '2024-03-01T00:00:00',
    updatedAt: '2024-03-01T00:00:00'
  }
];

const mockProducts = [
  {
    id: 1,
    name: '50元话费充值',
    productType: '话费充值',
    faceValue: 50,
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 2,
    name: '100元话费充值',
    productType: '话费充值',
    faceValue: 100,
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 3,
    name: '200元话费充值',
    productType: '话费充值',
    faceValue: 200,
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  }
];

const mockSupplierProducts = [
  {
    id: 1,
    supplierId: 1,
    productId: 1,
    productName: '50元话费充值',
    productType: '话费充值',
    productFaceValue: 50,
    supplierProductCode: 'CMCC-P001',
    supplierPrice: 48.5,
    faceValue: 50,
    stockQuantity: 1000,
    dailyStockLimit: 100,
    stockAmount: 48500,
    salesQuantity: 0,
    salesAmount: 0,
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 2,
    supplierId: 1,
    productId: 2,
    productName: '100元话费充值',
    productType: '话费充值',
    productFaceValue: 100,
    supplierProductCode: 'CMCC-P002',
    supplierPrice: 97,
    faceValue: 100,
    stockQuantity: 500,
    dailyStockLimit: 50,
    stockAmount: 48500,
    salesQuantity: 0,
    salesAmount: 0,
    enabled: true,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 3,
    supplierId: 2,
    productId: 1,
    productName: '50元话费充值',
    productType: '话费充值',
    productFaceValue: 50,
    supplierProductCode: 'CUCC-P001',
    supplierPrice: 48.3,
    faceValue: 50,
    stockQuantity: 800,
    dailyStockLimit: 80,
    stockAmount: 38640,
    salesQuantity: 0,
    salesAmount: 0,
    enabled: true,
    createdAt: '2024-02-01T00:00:00',
    updatedAt: '2024-02-01T00:00:00'
  }
];

const mockSupplierBalances = [
  {
    id: 1,
    supplierId: 1,
    totalRecharge: 100000,
    consumedAmount: 35000,
    remainingAmount: 65000,
    alertThreshold: 10000,
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00'
  },
  {
    id: 2,
    supplierId: 2,
    totalRecharge: 80000,
    consumedAmount: 20000,
    remainingAmount: 60000,
    alertThreshold: 10000,
    createdAt: '2024-02-01T00:00:00',
    updatedAt: '2024-02-01T00:00:00'
  },
  {
    id: 3,
    supplierId: 3,
    totalRecharge: 50000,
    consumedAmount: 10000,
    remainingAmount: 40000,
    alertThreshold: 10000,
    createdAt: '2024-03-01T00:00:00',
    updatedAt: '2024-03-01T00:00:00'
  }
];

const mockSupplierRecharges = [
  {
    id: 1,
    supplierId: 1,
    amount: 50000,
    screenshotUrl: '/uploads/screenshot1.png',
    operatorId: 1,
    operatorName: 'admin',
    enabled: true,
    createdAt: '2024-01-15T10:00:00',
    updatedAt: '2024-01-15T10:00:00'
  },
  {
    id: 2,
    supplierId: 1,
    amount: 50000,
    screenshotUrl: '/uploads/screenshot2.png',
    operatorId: 1,
    operatorName: 'admin',
    enabled: true,
    createdAt: '2024-02-15T10:00:00',
    updatedAt: '2024-02-15T10:00:00'
  },
  {
    id: 3,
    supplierId: 2,
    amount: 80000,
    screenshotUrl: '/uploads/screenshot3.png',
    operatorId: 1,
    operatorName: 'admin',
    enabled: true,
    createdAt: '2024-02-01T10:00:00',
    updatedAt: '2024-02-01T10:00:00'
  }
];

// 模拟登录接口
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body;
  
  if (username === 'admin' && password === 'admin123') {
    res.json({
      token: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlkIjoxLCJlbWFpbCI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzczNTU3NDk0LCJleHAiOjE3NzM2NDM4OTR9.1BumlfAA4MvwqhpC3FNQKMc0MJK57TAq6XULCD_KFU4',
      type: 'Bearer',
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_ADMIN']
    });
  } else {
    res.status(401).json({
      message: '用户名或密码错误'
    });
  }
});

// 供应商接口
app.get('/api/suppliers', (req, res) => {
  res.json(mockSuppliers);
});

app.post('/api/suppliers', (req, res) => {
  const newSupplier = {
    id: mockSuppliers.length + 1,
    ...req.body,
    supplierCode: `SUP${String(mockSuppliers.length + 1).padStart(3, '0')}`,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  mockSuppliers.push(newSupplier);
  res.json(newSupplier);
});

app.get('/api/suppliers/:id', (req, res) => {
  const supplier = mockSuppliers.find(s => s.id === parseInt(req.params.id));
  if (supplier) {
    res.json(supplier);
  } else {
    res.status(404).json({ message: '供应商不存在' });
  }
});

app.put('/api/suppliers/:id', (req, res) => {
  const index = mockSuppliers.findIndex(s => s.id === parseInt(req.params.id));
  if (index !== -1) {
    mockSuppliers[index] = { ...mockSuppliers[index], ...req.body, updatedAt: new Date().toISOString() };
    res.json(mockSuppliers[index]);
  } else {
    res.status(404).json({ message: '供应商不存在' });
  }
});

app.delete('/api/suppliers/:id', (req, res) => {
  const index = mockSuppliers.findIndex(s => s.id === parseInt(req.params.id));
  if (index !== -1) {
    mockSuppliers.splice(index, 1);
    res.json({ message: '删除成功' });
  } else {
    res.status(404).json({ message: '供应商不存在' });
  }
});

// 产品接口
app.get('/api/products', (req, res) => {
  res.json(mockProducts);
});

// 供应商产品接口
app.get('/api/supplier-products', (req, res) => {
  res.json(mockSupplierProducts);
});

app.post('/api/supplier-products', (req, res) => {
  const product = mockProducts.find(p => p.id === req.body.productId);
  const newSupplierProduct = {
    id: mockSupplierProducts.length + 1,
    ...req.body,
    productName: product?.name || '',
    productType: product?.productType || '',
    productFaceValue: product?.faceValue || 0,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  mockSupplierProducts.push(newSupplierProduct);
  res.json(newSupplierProduct);
});

app.put('/api/supplier-products/:id', (req, res) => {
  const index = mockSupplierProducts.findIndex(s => s.id === parseInt(req.params.id));
  if (index !== -1) {
    mockSupplierProducts[index] = { ...mockSupplierProducts[index], ...req.body, updatedAt: new Date().toISOString() };
    res.json(mockSupplierProducts[index]);
  } else {
    res.status(404).json({ message: '供应商产品不存在' });
  }
});

// 供应商余额接口
app.get('/api/supplier-balances/supplier/:supplierId', (req, res) => {
  const balance = mockSupplierBalances.find(b => b.supplierId === parseInt(req.params.supplierId));
  if (balance) {
    res.json(balance);
  } else {
    res.json({
      supplierId: parseInt(req.params.supplierId),
      totalRecharge: 0,
      consumedAmount: 0,
      remainingAmount: 0,
      alertThreshold: 10000
    });
  }
});

app.put('/api/supplier-balances/supplier/:supplierId', (req, res) => {
  const index = mockSupplierBalances.findIndex(b => b.supplierId === parseInt(req.params.supplierId));
  if (index !== -1) {
    mockSupplierBalances[index] = { ...mockSupplierBalances[index], ...req.body, updatedAt: new Date().toISOString() };
    res.json(mockSupplierBalances[index]);
  } else {
    const newBalance = {
      id: mockSupplierBalances.length + 1,
      supplierId: parseInt(req.params.supplierId),
      ...req.body,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    mockSupplierBalances.push(newBalance);
    res.json(newBalance);
  }
});

// 供应商充值记录接口
app.get('/api/supplier-recharges', (req, res) => {
  res.json(mockSupplierRecharges);
});

app.post('/api/supplier-recharges', (req, res) => {
  const newRecharge = {
    id: mockSupplierRecharges.length + 1,
    ...req.body,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  mockSupplierRecharges.push(newRecharge);
  res.json(newRecharge);
});

// 文件上传接口
app.post('/api/upload', (req, res) => {
  res.json({
    url: '/uploads/test.png'
  });
});

app.listen(port, () => {
  console.log(`Mock backend server running at http://localhost:${port}`);
});
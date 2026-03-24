const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

app.use(cors({
  origin: ['http://localhost:5173', 'http://localhost:5174', 'http://localhost:5175', 'http://localhost:5176', 'http://localhost:5177', 'http://localhost:5178'],
  credentials: true
}));
app.use(express.json());

// 模拟登录接口
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body;
  
  // 模拟验证逻辑
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

app.listen(port, () => {
  console.log(`Mock backend server running at http://localhost:${port}`);
});

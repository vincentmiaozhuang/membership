const mysql = require('mysql2/promise');

// 数据库连接配置
const dbConfig = {
  host: 'localhost',
  user: 'members',
  password: 'members',
  database: 'membership_db'
};

// 生成随机字符串
function generateRandomString(length) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

// 生成随机手机号
function generateRandomPhone() {
  return '1' + Math.floor(Math.random() * 9000000000 + 1000000000);
}

// 生成昨天的日期
function getYesterday() {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  return yesterday;
}

// 格式化日期为YYYY-MM-DD HH:mm:ss
function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// 生成充值记录
async function generateRechargeRecords() {
  let connection;
  try {
    // 连接数据库
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database');

    // 查询已有的产品、客户、供应商数据
    const [products] = await connection.execute('SELECT id, name, face_price FROM products WHERE enabled = true');
    const [customers] = await connection.execute('SELECT id, name FROM customers WHERE enabled = true');
    const [suppliers] = await connection.execute('SELECT id, name FROM suppliers WHERE enabled = true');

    if (products.length === 0 || customers.length === 0 || suppliers.length === 0) {
      console.error('Error: No data found in products, customers or suppliers table');
      return;
    }

    console.log(`Found ${products.length} products, ${customers.length} customers, ${suppliers.length} suppliers`);

    // 生成100条充值记录
    const records = [];
    const yesterday = getYesterday();

    for (let i = 0; i < 100; i++) {
      // 随机选择产品、客户、供应商
      const product = products[Math.floor(Math.random() * products.length)];
      const customer = customers[Math.floor(Math.random() * customers.length)];
      const supplier = suppliers[Math.floor(Math.random() * suppliers.length)];

      // 随机生成价格（在产品面值的基础上上下浮动）
      const facePrice = product.face_price;
      const customerPrice = facePrice * (1 + (Math.random() * 0.2 - 0.1)); // 上下浮动10%
      const supplierPrice = facePrice * (1 + (Math.random() * 0.1 - 0.05)); // 上下浮动5%

      // 随机状态
      const statuses = ['success', 'failed', 'pending'];
      const status = statuses[Math.floor(Math.random() * statuses.length)];

      // 生成订单ID
      const customerOrderId = generateRandomString(16);
      const platformOrderId = generateRandomString(20);

      // 生成记录
      records.push([
        product.id,
        product.name,
        product.face_price,
        generateRandomPhone(),
        customerOrderId,
        platformOrderId,
        customer.id,
        customer.name,
        customerPrice.toFixed(2),
        supplier.id,
        supplier.name,
        supplierPrice.toFixed(2),
        status,
        `Test recharge record ${i + 1}`,
        formatDate(new Date(yesterday.getTime() + Math.random() * 86400000)), // 昨天的随机时间
        0 // 系统充值
      ]);
    }

    // 批量插入记录
    const query = `
      INSERT INTO recharge_records (
        product_id, product_name, product_face_price, recharge_phone, 
        customer_order_id, platform_order_id, customer_id, customer_name, 
        customer_price, supplier_id, supplier_name, supplier_price, 
        status, description, created_at, recharge_person
      ) VALUES ?
    `;

    const [result] = await connection.execute(query, [records]);
    console.log(`Successfully inserted ${result.affectedRows} recharge records`);

  } catch (error) {
    console.error('Error generating recharge records:', error);
  } finally {
    if (connection) {
      await connection.end();
      console.log('Database connection closed');
    }
  }
}

// 运行生成脚本
generateRechargeRecords();
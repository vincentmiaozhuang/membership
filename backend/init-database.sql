-- 会员卡管理系统数据库初始化脚本
-- 生成日期：2026-05-17
-- 基于当前实体类结构生成



-- 创建数据库
CREATE DATABASE IF NOT EXISTS membership_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE membership_db;

-- 禁用外键检查，方便初始化
SET FOREIGN_KEY_CHECKS = 0;

-- 删除现有表（按依赖顺序）
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS recharge_stats;
DROP TABLE IF EXISTS recharge_records;
DROP TABLE IF EXISTS customer_product_supplier_orders;
DROP TABLE IF EXISTS customer_products;
DROP TABLE IF EXISTS supplier_products;
DROP TABLE IF EXISTS customer_balances;
DROP TABLE IF EXISTS supplier_balances;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================
-- 1. 用户表 users
-- ==============================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==============================================
-- 2. 角色表 roles
-- ==============================================
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ==============================================
-- 3. 权限表 permissions
-- ==============================================
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ==============================================
-- 4. 用户角色关联表 user_roles
-- ==============================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ==============================================
-- 5. 角色权限关联表 role_permissions
-- ==============================================
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ==============================================
-- 6. 供应商表 suppliers
-- ==============================================
CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    supplier_code VARCHAR(16) NOT NULL UNIQUE,
    cooperation_start_date DATETIME,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ==============================================
-- 7. 产品表 products
-- ==============================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    face_value DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- ==============================================
-- 8. 客户表 customers
-- ==============================================
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    customer_code VARCHAR(16) NOT NULL UNIQUE,
    customer_secret VARCHAR(16) NOT NULL,
    ip_whitelist TEXT,
    cooperation_start_date DATETIME,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ==============================================
-- 9. 供应商产品表 supplier_products
-- ==============================================
CREATE TABLE supplier_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    supplier_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    sales_quantity INT NOT NULL DEFAULT 0,
    stock_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    sales_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    supplier_product_code VARCHAR(16) NOT NULL UNIQUE,
    face_value DECIMAL(10,2),
    daily_stock_limit INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_supplier_product (supplier_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商产品表';

-- ==============================================
-- 10. 客户产品表 customer_products
-- ==============================================
CREATE TABLE customer_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    customer_product_code VARCHAR(32) NOT NULL UNIQUE,
    customer_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    shipment_quantity INT NOT NULL DEFAULT 0,
    stock_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    shipment_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_product (customer_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户产品表';

-- ==============================================
-- 11. 客户产品供应商顺序表 customer_product_supplier_orders
-- ==============================================
CREATE TABLE customer_product_supplier_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (customer_product_id) REFERENCES customer_products(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_cp_supplier (customer_product_id, supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户产品供应商顺序表';

-- ==============================================
-- 12. 客户余额表 customer_balances
-- ==============================================
CREATE TABLE customer_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL UNIQUE,
    payment_account_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_account_consumed DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_account_balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    credit_account_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    credit_account_consumed DECIMAL(10,2) NOT NULL DEFAULT 0,
    credit_account_balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_account_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_account_consumed DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_account_balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    alert_threshold DECIMAL(10,2) NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    vip_pass_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户余额表';

-- ==============================================
-- 13. 供应商余额表 supplier_balances
-- ==============================================
CREATE TABLE supplier_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL UNIQUE,
    total_recharge DECIMAL(10,2) NOT NULL DEFAULT 0,
    consumed_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    alert_threshold DECIMAL(10,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商余额表';

-- ==============================================
-- 14. 充值记录表 recharge_records
-- ==============================================
CREATE TABLE recharge_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_product_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(100),
    recharge_phone VARCHAR(20) NOT NULL,
    customer_order_id VARCHAR(50),
    platform_order_id VARCHAR(50),
    customer_id BIGINT,
    customer_name VARCHAR(100),
    customer_price DECIMAL(10,2),
    supplier_id BIGINT,
    supplier_name VARCHAR(100),
    supplier_price DECIMAL(10,2),
    product_face_price DECIMAL(10,2),
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    recharge_person BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (customer_product_id) REFERENCES customer_products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- ==============================================
-- 15. 充值统计表 recharge_stats
-- ==============================================
CREATE TABLE recharge_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    recharge_count INT DEFAULT 0,
    customer_price DECIMAL(10,2) NOT NULL,
    customer_amount DECIMAL(18,2) DEFAULT 0,
    supplier_price DECIMAL(10,2) NOT NULL,
    cost_amount DECIMAL(18,2) DEFAULT 0,
    product_face_price DECIMAL(10,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_dimension_status (stat_date, product_id, customer_id, supplier_id, status),
    INDEX idx_stat_date (stat_date),
    INDEX idx_product (product_id, stat_date),
    INDEX idx_customer (customer_id, stat_date),
    INDEX idx_supplier (supplier_id, stat_date),
    INDEX idx_status (status, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值统计表';

-- ==============================================
-- 初始化默认数据
-- ==============================================

-- 插入默认权限
INSERT INTO permissions (name, resource, action, description, created_at, updated_at) VALUES
('user:read', 'user', 'read', '查看用户', NOW(), NOW()),
('user:create', 'user', 'create', '创建用户', NOW(), NOW()),
('user:update', 'user', 'update', '更新用户', NOW(), NOW()),
('user:delete', 'user', 'delete', '删除用户', NOW(), NOW()),
('role:read', 'role', 'read', '查看角色', NOW(), NOW()),
('role:create', 'role', 'create', '创建角色', NOW(), NOW()),
('role:update', 'role', 'update', '更新角色', NOW(), NOW()),
('role:delete', 'role', 'delete', '删除角色', NOW(), NOW()),
('permission:read', 'permission', 'read', '查看权限', NOW(), NOW()),
('permission:create', 'permission', 'create', '创建权限', NOW(), NOW()),
('permission:update', 'permission', 'update', '更新权限', NOW(), NOW()),
('permission:delete', 'permission', 'delete', '删除权限', NOW(), NOW()),
('supplier:read', 'supplier', 'read', '查看供应商', NOW(), NOW()),
('supplier:create', 'supplier', 'create', '创建供应商', NOW(), NOW()),
('supplier:update', 'supplier', 'update', '更新供应商', NOW(), NOW()),
('supplier:delete', 'supplier', 'delete', '删除供应商', NOW(), NOW()),
('product:read', 'product', 'read', '查看产品', NOW(), NOW()),
('product:create', 'product', 'create', '创建产品', NOW(), NOW()),
('product:update', 'product', 'update', '更新产品', NOW(), NOW()),
('product:delete', 'product', 'delete', '删除产品', NOW(), NOW()),
('customer:read', 'customer', 'read', '查看客户', NOW(), NOW()),
('customer:create', 'customer', 'create', '创建客户', NOW(), NOW()),
('customer:update', 'customer', 'update', '更新客户', NOW(), NOW()),
('customer:delete', 'customer', 'delete', '删除客户', NOW(), NOW()),
('recharge:read', 'recharge', 'read', '查看充值记录', NOW(), NOW()),
('recharge:create', 'recharge', 'create', '创建充值记录', NOW(), NOW()),
('recharge:update', 'recharge', 'update', '更新充值记录', NOW(), NOW()),
('recharge:delete', 'recharge', 'delete', '删除充值记录', NOW(), NOW()),
('stats:read', 'stats', 'read', '查看统计数据', NOW(), NOW()),
('daily-stats:read', 'daily-stats', 'read', '查看每日充值汇总', NOW(), NOW());

-- 插入默认角色
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ROLE_ADMIN', '管理员角色', NOW(), NOW()),
('ROLE_USER', '普通用户角色', NOW(), NOW()),
('ROLE_MANAGER', '经理角色', NOW(), NOW());

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN';

-- 为经理角色分配部分权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ROLE_MANAGER' AND p.name IN (
    'supplier:read', 'product:read', 'customer:read', 'recharge:read', 'stats:read', 'daily-stats:read'
);

-- 为普通用户角色分配基础权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ROLE_USER' AND p.name IN (
    'supplier:read', 'product:read', 'customer:read', 'recharge:read'
);

-- 插入默认管理员用户（密码：admin123，使用BCrypt加密）
INSERT INTO users (username, password, email, real_name, phone, enabled, created_at, updated_at) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'admin@example.com', '管理员', '13800138000', TRUE, NOW(), NOW());

-- 为管理员用户分配管理员角色
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- 插入默认供应商
INSERT INTO suppliers (name, supplier_code, cooperation_start_date, enabled, created_at, updated_at) VALUES
('供应商A', 'SUPP001', NOW(), TRUE, NOW(), NOW()),
('供应商B', 'SUPP002', NOW(), TRUE, NOW(), NOW());

-- 插入默认产品
INSERT INTO products (name, type, face_value, description, enabled, created_at, updated_at) VALUES
('测试会员卡10元', '会员卡', 10.00, '测试用会员卡，面值10元', TRUE, NOW(), NOW()),
('测试会员卡30元', '会员卡', 30.00, '测试用会员卡，面值30元', TRUE, NOW(), NOW()),
('测试会员卡50元', '会员卡', 50.00, '测试用会员卡，面值50元', TRUE, NOW(), NOW()),
('测试会员卡100元', '会员卡', 100.00, '测试用会员卡，面值100元', TRUE, NOW(), NOW());

-- 插入默认客户
INSERT INTO customers (name, customer_code, customer_secret, ip_whitelist, cooperation_start_date, enabled, created_at, updated_at) VALUES
('客户A', 'CUST001', 'secret001', '192.168.1.1', NOW(), TRUE, NOW(), NOW()),
('客户B', 'CUST002', 'secret002', '192.168.1.2', NOW(), TRUE, NOW(), NOW());

-- 插入供应商产品
INSERT INTO supplier_products (product_id, supplier_id, supplier_price, stock_quantity, sales_quantity, stock_amount, sales_amount, enabled, supplier_product_code, face_value, daily_stock_limit, created_at, updated_at) VALUES
((SELECT id FROM products WHERE name = '测试会员卡10元'), (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), 9.00, 1000, 0, 9000.00, 0.00, TRUE, 'SP001001', 10.00, 100, NOW(), NOW()),
((SELECT id FROM products WHERE name = '测试会员卡30元'), (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), 27.00, 1000, 0, 27000.00, 0.00, TRUE, 'SP001002', 30.00, 100, NOW(), NOW()),
((SELECT id FROM products WHERE name = '测试会员卡50元'), (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), 45.00, 1000, 0, 45000.00, 0.00, TRUE, 'SP002001', 50.00, 100, NOW(), NOW()),
((SELECT id FROM products WHERE name = '测试会员卡100元'), (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), 90.00, 1000, 0, 90000.00, 0.00, TRUE, 'SP002002', 100.00, 100, NOW(), NOW());

-- 为客户分配产品
INSERT INTO customer_products (customer_id, product_id, customer_product_code, customer_price, stock_quantity, shipment_quantity, stock_amount, shipment_amount, enabled, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE customer_code = 'CUST001'), (SELECT id FROM products WHERE name = '测试会员卡10元'), 'CP001001', 9.50, 100, 0, 950.00, 0.00, TRUE, NOW(), NOW()),
((SELECT id FROM customers WHERE customer_code = 'CUST001'), (SELECT id FROM products WHERE name = '测试会员卡30元'), 'CP001002', 28.50, 100, 0, 2850.00, 0.00, TRUE, NOW(), NOW()),
((SELECT id FROM customers WHERE customer_code = 'CUST002'), (SELECT id FROM products WHERE name = '测试会员卡50元'), 'CP002001', 47.50, 100, 0, 4750.00, 0.00, TRUE, NOW(), NOW()),
((SELECT id FROM customers WHERE customer_code = 'CUST002'), (SELECT id FROM products WHERE name = '测试会员卡100元'), 'CP002002', 95.00, 100, 0, 9500.00, 0.00, TRUE, NOW(), NOW());

-- 插入客户余额
INSERT INTO customer_balances (customer_id, payment_account_total, payment_account_consumed, payment_account_balance, credit_account_total, credit_account_consumed, credit_account_balance, total_account_amount, total_account_consumed, total_account_balance, alert_threshold, enabled, vip_pass_enabled, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE customer_code = 'CUST001'), 10000.00, 0.00, 10000.00, 5000.00, 0.00, 5000.00, 15000.00, 0.00, 15000.00, 1000.00, TRUE, FALSE, NOW(), NOW()),
((SELECT id FROM customers WHERE customer_code = 'CUST002'), 20000.00, 0.00, 20000.00, 10000.00, 0.00, 10000.00, 30000.00, 0.00, 30000.00, 2000.00, TRUE, TRUE, NOW(), NOW());

-- 插入供应商余额
INSERT INTO supplier_balances (supplier_id, total_recharge, consumed_amount, remaining_amount, alert_threshold, created_at, updated_at) VALUES
((SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), 50000.00, 0.00, 50000.00, 5000.00, NOW(), NOW()),
((SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), 80000.00, 0.00, 80000.00, 8000.00, NOW(), NOW());

-- 插入测试充值记录
INSERT INTO recharge_records (customer_product_id, product_id, product_name, recharge_phone, customer_order_id, platform_order_id, customer_id, customer_name, customer_price, supplier_id, supplier_name, supplier_price, product_face_price, status, description, recharge_person, created_at, updated_at) VALUES
((SELECT id FROM customer_products WHERE customer_product_code = 'CP001001'), (SELECT id FROM products WHERE name = '测试会员卡10元'), '测试会员卡10元', '13800138001', 'CUST001_001', 'PLAT001', (SELECT id FROM customers WHERE customer_code = 'CUST001'), '客户A', 9.50, (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), '供应商A', 9.00, 10.00, '成功', '测试充值记录1', 1, NOW(), NOW()),
((SELECT id FROM customer_products WHERE customer_product_code = 'CP001002'), (SELECT id FROM products WHERE name = '测试会员卡30元'), '测试会员卡30元', '13800138002', 'CUST001_002', 'PLAT002', (SELECT id FROM customers WHERE customer_code = 'CUST001'), '客户A', 28.50, (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), '供应商A', 27.00, 30.00, '成功', '测试充值记录2', 1, NOW(), NOW()),
((SELECT id FROM customer_products WHERE customer_product_code = 'CP002001'), (SELECT id FROM products WHERE name = '测试会员卡50元'), '测试会员卡50元', '13800138003', 'CUST002_001', 'PLAT003', (SELECT id FROM customers WHERE customer_code = 'CUST002'), '客户B', 47.50, (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), '供应商B', 45.00, 50.00, '成功', '测试充值记录3', 1, NOW(), NOW()),
((SELECT id FROM customer_products WHERE customer_product_code = 'CP002002'), (SELECT id FROM products WHERE name = '测试会员卡100元'), '测试会员卡100元', '13800138004', 'CUST002_002', 'PLAT004', (SELECT id FROM customers WHERE customer_code = 'CUST002'), '客户B', 95.00, (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), '供应商B', 90.00, 100.00, '失败', '测试充值记录4（失败）', 1, NOW(), NOW());

-- 插入测试充值统计数据
INSERT INTO recharge_stats (stat_date, product_id, product_name, customer_id, customer_name, supplier_id, supplier_name, status, recharge_count, customer_price, customer_amount, supplier_price, cost_amount, product_face_price, created_at, updated_at) VALUES
(CURDATE(), (SELECT id FROM products WHERE name = '测试会员卡10元'), '测试会员卡10元', (SELECT id FROM customers WHERE customer_code = 'CUST001'), '客户A', (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), '供应商A', '成功', 1, 9.50, 9.50, 9.00, 9.00, 10.00, NOW(), NOW()),
(CURDATE(), (SELECT id FROM products WHERE name = '测试会员卡30元'), '测试会员卡30元', (SELECT id FROM customers WHERE customer_code = 'CUST001'), '客户A', (SELECT id FROM suppliers WHERE supplier_code = 'SUPP001'), '供应商A', '成功', 1, 28.50, 28.50, 27.00, 27.00, 30.00, NOW(), NOW()),
(CURDATE(), (SELECT id FROM products WHERE name = '测试会员卡50元'), '测试会员卡50元', (SELECT id FROM customers WHERE customer_code = 'CUST002'), '客户B', (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), '供应商B', '成功', 1, 47.50, 47.50, 45.00, 45.00, 50.00, NOW(), NOW()),
(CURDATE(), (SELECT id FROM products WHERE name = '测试会员卡100元'), '测试会员卡100元', (SELECT id FROM customers WHERE customer_code = 'CUST002'), '客户B', (SELECT id FROM suppliers WHERE supplier_code = 'SUPP002'), '供应商B', '失败', 1, 95.00, 95.00, 90.00, 90.00, 100.00, NOW(), NOW());

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示初始化结果
SELECT '数据库初始化完成' AS result;
SELECT '默认管理员账号：admin / admin123' AS admin_account;

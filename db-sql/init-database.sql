-- 会员管理系统数据库初始化脚本
-- 生成日期：2026-05-19
-- 基于当前生产数据库结构生成

-- 创建数据库
CREATE DATABASE IF NOT EXISTS membership_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE membership_db;

-- 禁用外键检查，方便初始化
SET FOREIGN_KEY_CHECKS = 0;

-- 删除现有表（按依赖顺序）
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS recharge_stats;
DROP TABLE IF EXISTS recharge_records;
DROP TABLE IF EXISTS customer_payments;
DROP TABLE IF EXISTS supplier_recharges;
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
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    real_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==============================================
-- 2. 角色表 roles
-- ==============================================
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ==============================================
-- 3. 权限表 permissions
-- ==============================================
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    resource VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
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
    name VARCHAR(255) NOT NULL,
    supplier_code VARCHAR(16) NOT NULL UNIQUE,
    cooperation_start_date DATETIME,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ==============================================
-- 7. 产品表 products
-- ==============================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(255),
    face_value DECIMAL(10,2),
    description VARCHAR(255),
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- ==============================================
-- 8. 客户表 customers
-- ==============================================
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    customer_code VARCHAR(16) NOT NULL UNIQUE,
    customer_secret VARCHAR(16) NOT NULL,
    ip_whitelist VARCHAR(255),
    cooperation_start_date DATETIME,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ==============================================
-- 9. 供应商产品表 supplier_products
-- ==============================================
CREATE TABLE supplier_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    supplier_product_code VARCHAR(16),
    supplier_price DECIMAL(10,2),
    stock_quantity INT DEFAULT 0,
    sales_quantity INT DEFAULT 0,
    stock_amount DECIMAL(10,2),
    sales_amount DECIMAL(10,2),
    face_value DECIMAL(10,2),
    daily_stock_limit INT,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
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
    customer_product_code VARCHAR(32) NOT NULL,
    customer_price DECIMAL(10,2),
    stock_quantity INT DEFAULT 0,
    shipment_quantity INT DEFAULT 0,
    stock_amount DECIMAL(10,2),
    shipment_amount DECIMAL(10,2),
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
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
    order_index INT,
    FOREIGN KEY (customer_product_id) REFERENCES customer_products(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_cp_supplier (customer_product_id, supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户产品供应商顺序表';

-- ==============================================
-- 12. 客户余额表 customer_balances
-- ==============================================
CREATE TABLE customer_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    payment_account_total DECIMAL(10,2),
    payment_account_consumed DECIMAL(10,2),
    payment_account_balance DECIMAL(10,2),
    credit_account_total DECIMAL(10,2),
    credit_account_consumed DECIMAL(10,2),
    credit_account_balance DECIMAL(10,2),
    total_account_amount DECIMAL(10,2),
    total_account_consumed DECIMAL(10,2),
    total_account_balance DECIMAL(10,2),
    alert_threshold DECIMAL(10,2),
    enabled BIT(1) NOT NULL DEFAULT b'1',
    vip_pass_enabled BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_balance (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户余额表';

-- ==============================================
-- 13. 供应商余额表 supplier_balances
-- ==============================================
CREATE TABLE supplier_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    total_recharge DECIMAL(10,2),
    consumed_amount DECIMAL(10,2),
    remaining_amount DECIMAL(10,2),
    alert_threshold DECIMAL(10,2),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_supplier_balance (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商余额表';

-- ==============================================
-- 14. 充值记录表 recharge_records
-- ==============================================
CREATE TABLE recharge_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_product_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(255),
    recharge_phone VARCHAR(20),
    customer_order_id VARCHAR(50),
    platform_order_id VARCHAR(50),
    customer_id BIGINT,
    customer_name VARCHAR(255),
    customer_price DECIMAL(10,2),
    supplier_id BIGINT,
    supplier_name VARCHAR(255),
    supplier_price DECIMAL(10,2),
    product_face_price DECIMAL(10,2),
    status VARCHAR(20),
    description VARCHAR(500),
    recharge_person VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- ==============================================
-- 15. 充值统计表 recharge_stats
-- ==============================================
CREATE TABLE recharge_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    product_name VARCHAR(255),
    customer_id BIGINT,
    customer_name VARCHAR(255),
    supplier_id BIGINT,
    supplier_name VARCHAR(255),
    status VARCHAR(20),
    product_face_price DECIMAL(10,2),
    supplier_price DECIMAL(10,2),
    customer_price DECIMAL(10,2),
    recharge_count INT,
    cost_amount DECIMAL(10,2),
    customer_amount DECIMAL(10,2),
    stat_date DATE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值统计表';

-- ==============================================
-- 16. 客户付款记录表 customer_payments
-- ==============================================
CREATE TABLE customer_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    payment_amount DECIMAL(10,2),
    credit_amount DECIMAL(10,2),
    financial_screenshot VARCHAR(255),
    operator VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户付款记录表';

-- ==============================================
-- 17. 供应商充值记录表 supplier_recharges
-- ==============================================
CREATE TABLE supplier_recharges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    receipt_image VARCHAR(255),
    operator VARCHAR(255),
    status VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商充值记录表';

-- ==============================================
-- 18. 站内信表 messages
-- ==============================================
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';

-- ==============================================
-- 初始化基础数据
-- ==============================================

-- 插入默认角色
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ROLE_ADMIN', '系统管理员', NOW(6), NOW(6)),
(2, 'ROLE_MANAGER', '业务管理员', NOW(6), NOW(6)),
(3, 'ROLE_USER', '普通用户', NOW(6), NOW(6));

-- 插入默认权限
INSERT INTO permissions (id, name, resource, action, description, created_at, updated_at) VALUES
(1, 'user:read', 'user', 'read', '查看用户', NOW(6), NOW(6)),
(2, 'user:create', 'user', 'create', '创建用户', NOW(6), NOW(6)),
(3, 'user:update', 'user', 'update', '更新用户', NOW(6), NOW(6)),
(4, 'user:delete', 'user', 'delete', '删除用户', NOW(6), NOW(6)),
(5, 'role:read', 'role', 'read', '查看角色', NOW(6), NOW(6)),
(6, 'role:create', 'role', 'create', '创建角色', NOW(6), NOW(6)),
(7, 'role:update', 'role', 'update', '更新角色', NOW(6), NOW(6)),
(8, 'role:delete', 'role', 'delete', '删除角色', NOW(6), NOW(6)),
(9, 'permission:read', 'permission', 'read', '查看权限', NOW(6), NOW(6)),
(10, 'permission:create', 'permission', 'create', '创建权限', NOW(6), NOW(6)),
(11, 'permission:update', 'permission', 'update', '更新权限', NOW(6), NOW(6)),
(12, 'permission:delete', 'permission', 'delete', '删除权限', NOW(6), NOW(6)),
(13, 'supplier:read', 'supplier', 'read', '查看供应商', NOW(6), NOW(6)),
(14, 'supplier:create', 'supplier', 'create', '创建供应商', NOW(6), NOW(6)),
(15, 'supplier:update', 'supplier', 'update', '更新供应商', NOW(6), NOW(6)),
(16, 'supplier:delete', 'supplier', 'delete', '删除供应商', NOW(6), NOW(6)),
(17, 'supplier-product:read', 'supplier-product', 'read', '查看供应商产品', NOW(6), NOW(6)),
(18, 'supplier-product:create', 'supplier-product', 'create', '创建供应商产品', NOW(6), NOW(6)),
(19, 'supplier-product:update', 'supplier-product', 'update', '更新供应商产品', NOW(6), NOW(6)),
(20, 'supplier-product:delete', 'supplier-product', 'delete', '删除供应商产品', NOW(6), NOW(6)),
(21, 'supplier-balance:read', 'supplier-balance', 'read', '查看供应商余额', NOW(6), NOW(6)),
(22, 'supplier-balance:update', 'supplier-balance', 'update', '更新供应商余额', NOW(6), NOW(6)),
(23, 'product:read', 'product', 'read', '查看产品', NOW(6), NOW(6)),
(24, 'product:create', 'product', 'create', '创建产品', NOW(6), NOW(6)),
(25, 'product:update', 'product', 'update', '更新产品', NOW(6), NOW(6)),
(26, 'product:delete', 'product', 'delete', '删除产品', NOW(6), NOW(6)),
(27, 'customer:read', 'customer', 'read', '查看客户', NOW(6), NOW(6)),
(28, 'customer:create', 'customer', 'create', '创建客户', NOW(6), NOW(6)),
(29, 'customer:update', 'customer', 'update', '更新客户', NOW(6), NOW(6)),
(30, 'customer:delete', 'customer', 'delete', '删除客户', NOW(6), NOW(6)),
(31, 'customer-product:read', 'customer-product', 'read', '查看客户产品', NOW(6), NOW(6)),
(32, 'customer-product:create', 'customer-product', 'create', '创建客户产品', NOW(6), NOW(6)),
(33, 'customer-product:update', 'customer-product', 'update', '更新客户产品', NOW(6), NOW(6)),
(34, 'customer-product:delete', 'customer-product', 'delete', '删除客户产品', NOW(6), NOW(6)),
(35, 'customer-balance:read', 'customer-balance', 'read', '查看客户余额', NOW(6), NOW(6)),
(36, 'customer-balance:update', 'customer-balance', 'update', '更新客户余额', NOW(6), NOW(6)),
(37, 'customer-payment:read', 'customer-payment', 'read', '查看客户付款', NOW(6), NOW(6)),
(38, 'customer-payment:create', 'customer-payment', 'create', '创建客户付款', NOW(6), NOW(6)),
(39, 'recharge:read', 'recharge', 'read', '查看充值记录', NOW(6), NOW(6)),
(40, 'recharge:create', 'recharge', 'create', '创建充值记录', NOW(6), NOW(6));

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12),
(1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22),
(1, 23), (1, 24), (1, 25), (1, 26),
(1, 27), (1, 28), (1, 29), (1, 30),
(1, 31), (1, 32), (1, 33), (1, 34),
(1, 35), (1, 36),
(1, 37), (1, 38),
(1, 39), (1, 40);

-- 为业务管理员分配部分权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3),
(2, 13), (2, 14), (2, 15),
(2, 17), (2, 18), (2, 19),
(2, 21), (2, 22),
(2, 23), (2, 24), (2, 25),
(2, 27), (2, 28), (2, 29),
(2, 31), (2, 32), (2, 33),
(2, 35), (2, 36),
(2, 37), (2, 38),
(2, 39), (2, 40);

-- 为普通用户分配查看权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 1),
(3, 13),
(3, 23),
(3, 27),
(3, 31),
(3, 35),
(3, 37),
(3, 39);

-- 插入默认管理员用户 (密码: admin123, 已通过BCrypt加密)
INSERT INTO users (id, username, password, email, real_name, phone, enabled, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'admin@example.com', '系统管理员', '13800138000', b'1', NOW(6), NOW(6));

-- 为管理员分配管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- ==============================================
-- 创建索引（优化查询性能）
-- ==============================================
CREATE INDEX idx_recharge_records_customer_id ON recharge_records(customer_id);
CREATE INDEX idx_recharge_records_product_id ON recharge_records(product_id);
CREATE INDEX idx_recharge_records_status ON recharge_records(status);
CREATE INDEX idx_recharge_stats_stat_date ON recharge_stats(stat_date);
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);

-- ==============================================
-- 脚本执行完成
-- ==============================================
SELECT '数据库初始化完成' AS result;

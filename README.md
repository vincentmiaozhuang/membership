# 会员管理系统 (Membership Management System)

## 项目概述

本系统是一个会员管理系统，主要用于管理客户、供应商、产品以及充值记录等业务，提供完整的统计分析功能和站内信通知功能。

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Security | 5.7.x | 安全框架 |
| Spring Data JPA | 2.7.x | 数据访问层 |
| MySQL | 8.0+ | 数据库 |
| JWT | - | 身份认证 |
| SLF4J | 1.7.x | 日志框架 |

### 前端
| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| React | 17.0.2 | 前端框架 |
| Ant Design | 4.24.8 | UI组件库 |
| Vite | 2.9.16 | 构建工具 |
| React Router | 5.3.4 | 路由管理 |
| Axios | 0.27.2 | HTTP客户端 |
| xlsx | 0.18.5 | Excel处理 |

## 项目结构

```
membership-system/
├── backend/                    # 后端服务
│   ├── src/main/java/com/membership/
│   │   ├── controller/         # 控制器层 (19个控制器)
│   │   ├── service/            # 服务层
│   │   ├── repository/         # 数据访问层
│   │   ├── entity/             # 实体类 (15个实体)
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # 安全相关
│   │   ├── scheduler/          # 定时任务
│   │   └── util/               # 工具类
│   ├── src/main/resources/
│   │   └── application.yml     # 应用配置
│   ├── uploads/                # 文件上传目录
│   └── pom.xml                 # Maven配置
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── pages/              # 页面组件 (15个页面)
│   │   ├── components/         # 通用组件
│   │   ├── utils/              # 工具函数
│   │   └── contexts/           # 状态管理
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── log/                        # 日志目录
├── init-database.sql           # 数据库初始化脚本
├── init-database-20260519.sql # 数据库备份 (2026-05-19)
└── README.md                   # 项目说明文档
```

## 数据库表结构

### 数据库关系图
```
用户管理:     users ─── user_roles ─── roles ─── role_permissions ─── permissions
客户管理:     customers ─── customer_products ─── products
            └── customer_balances
            └── customer_payments
供应商管理:   suppliers ─── supplier_products ─── products
            └── supplier_balances
            └── supplier_recharges
充值业务:     recharge_records ─── customers, products, suppliers
统计分析:     recharge_stats
站内信:       messages ─── users
```

### 1. users（用户表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(255) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 邮箱 |
| real_name | VARCHAR(255) | NOT NULL | 真实姓名 |
| phone | VARCHAR(255) | NULL | 手机号 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 2. roles（角色表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 角色ID |
| name | VARCHAR(255) | NOT NULL, UNIQUE | 角色名称（如 ROLE_ADMIN, ROLE_USER） |
| description | VARCHAR(255) | NULL | 角色描述 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 3. permissions（权限表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 权限ID |
| name | VARCHAR(255) | NOT NULL, UNIQUE | 权限名称 |
| resource | VARCHAR(255) | NOT NULL | 资源名称（如 customer, product） |
| action | VARCHAR(255) | NOT NULL | 操作类型（如 read, create, update, delete） |
| description | VARCHAR(255) | NULL | 权限描述 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 4. role_permissions（角色权限关联表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| role_id | BIGINT | FOREIGN KEY | 角色ID |
| permission_id | BIGINT | FOREIGN KEY | 权限ID |

### 5. user_roles（用户角色关联表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| role_id | BIGINT | FOREIGN KEY | 角色ID |

### 6. products（产品表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 产品ID |
| name | VARCHAR(255) | NOT NULL, UNIQUE | 产品名称 |
| type | VARCHAR(255) | NULL | 产品类型 |
| face_value | DECIMAL(10,2) | NULL | 面值 |
| description | VARCHAR(255) | NULL | 描述 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 7. suppliers（供应商表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 供应商ID |
| name | VARCHAR(255) | NOT NULL | 供应商名称 |
| supplier_code | VARCHAR(16) | NOT NULL, UNIQUE | 供应商编码 |
| cooperation_start_date | DATETIME | NULL | 合作开始日期 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 8. customers（客户表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 客户ID |
| name | VARCHAR(255) | NOT NULL | 客户名称 |
| customer_code | VARCHAR(16) | NOT NULL, UNIQUE | 客户编码 |
| customer_secret | VARCHAR(16) | NOT NULL | 客户秘钥 |
| ip_whitelist | VARCHAR(255) | NULL | IP白名单 |
| cooperation_start_date | DATETIME | NULL | 合作开始日期 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 9. supplier_products（供应商产品表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| product_id | BIGINT | FOREIGN KEY, NOT NULL | 产品ID |
| supplier_id | BIGINT | FOREIGN KEY, NOT NULL | 供应商ID |
| supplier_product_code | VARCHAR(16) | NULL | 供应商产品码 |
| supplier_price | DECIMAL(10,2) | NULL | 供应商价格 |
| stock_quantity | INT | NULL | 库存量 |
| sales_quantity | INT | NULL | 出货量 |
| stock_amount | DECIMAL(10,2) | NULL | 库存金额 |
| sales_amount | DECIMAL(10,2) | NULL | 出货金额 |
| face_value | DECIMAL(10,2) | NULL | 面值 |
| daily_stock_limit | INT | NULL | 每日库存限量 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 10. customer_products（客户产品表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| customer_id | BIGINT | FOREIGN KEY, NOT NULL | 客户ID |
| product_id | BIGINT | FOREIGN KEY, NOT NULL | 产品ID |
| customer_product_code | VARCHAR(32) | NOT NULL | 客户产品码 |
| customer_price | DECIMAL(10,2) | NULL | 客户价格 |
| stock_quantity | INT | NULL | 库存量 |
| shipment_quantity | INT | NULL | 出货量 |
| stock_amount | DECIMAL(10,2) | NULL | 库存金额 |
| shipment_amount | DECIMAL(10,2) | NULL | 出货金额 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 11. customer_product_supplier_orders（客户产品供应商顺序表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| customer_product_id | BIGINT | FOREIGN KEY, NOT NULL | 客户产品ID |
| supplier_id | BIGINT | FOREIGN KEY, NOT NULL | 供应商ID |
| order_index | INT | NULL | 顺序索引 |

### 12. customer_balances（客户余额表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| customer_id | BIGINT | FOREIGN KEY, NOT NULL | 客户ID |
| payment_account_total | DECIMAL(10,2) | NULL | 付款账户总额 |
| payment_account_consumed | DECIMAL(10,2) | NULL | 付款账户已消费 |
| payment_account_balance | DECIMAL(10,2) | NULL | 付款账户余额 |
| credit_account_total | DECIMAL(10,2) | NULL | 授信账户总额 |
| credit_account_consumed | DECIMAL(10,2) | NULL | 授信账户已消费 |
| credit_account_balance | DECIMAL(10,2) | NULL | 授信账户余额 |
| total_account_amount | DECIMAL(10,2) | NULL | 账户总计总额 |
| total_account_consumed | DECIMAL(10,2) | NULL | 账户总计已消费 |
| total_account_balance | DECIMAL(10,2) | NULL | 账户总计余额 |
| alert_threshold | DECIMAL(10,2) | NULL | 预警阈值 |
| enabled | BIT(1) | NOT NULL | 是否启用 |
| vip_pass_enabled | BIT(1) | NOT NULL | 大客户通行状态 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 13. customer_payments（客户付款记录表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| customer_id | BIGINT | FOREIGN KEY, NOT NULL | 客户ID |
| payment_amount | DECIMAL(10,2) | NULL | 付款金额 |
| credit_amount | DECIMAL(10,2) | NULL | 授信金额 |
| financial_screenshot | VARCHAR(255) | NULL | 财务凭证截图路径 |
| operator | VARCHAR(255) | NULL | 操作人 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 14. supplier_balances（供应商余额表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| supplier_id | BIGINT | FOREIGN KEY, NOT NULL | 供应商ID |
| total_recharge | DECIMAL(10,2) | NULL | 充值总额 |
| consumed_amount | DECIMAL(10,2) | NULL | 已消费金额 |
| remaining_amount | DECIMAL(10,2) | NULL | 剩余金额 |
| alert_threshold | DECIMAL(10,2) | NULL | 预警阈值 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 15. supplier_recharges（供应商充值记录表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| supplier_id | BIGINT | FOREIGN KEY, NOT NULL | 供应商ID |
| amount | DECIMAL(10,2) | NULL | 充值金额 |
| payment_method | VARCHAR(50) | NULL | 支付方式 |
| receipt_image | VARCHAR(255) | NULL | 收款凭证图片 |
| operator | VARCHAR(255) | NULL | 操作人 |
| status | VARCHAR(20) | NULL | 状态 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 16. recharge_records（充值记录表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| customer_product_id | BIGINT | NULL | 客户产品ID |
| product_id | BIGINT | NULL | 产品ID |
| product_name | VARCHAR(255) | NULL | 产品名称 |
| recharge_phone | VARCHAR(20) | NULL | 充值手机号 |
| customer_order_id | VARCHAR(50) | NULL | 客户订单ID |
| platform_order_id | VARCHAR(50) | NULL | 平台订单ID |
| customer_id | BIGINT | NULL | 客户ID |
| customer_name | VARCHAR(255) | NULL | 客户名称 |
| customer_price | DECIMAL(10,2) | NULL | 客户价格 |
| supplier_id | BIGINT | NULL | 供应商ID |
| supplier_name | VARCHAR(255) | NULL | 供应商名称 |
| supplier_price | DECIMAL(10,2) | NULL | 供应商价格 |
| product_face_price | DECIMAL(10,2) | NULL | 产品票面价格 |
| status | VARCHAR(20) | NULL | 状态 |
| description | VARCHAR(500) | NULL | 描述 |
| recharge_person | VARCHAR(255) | NULL | 充值人 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 17. recharge_stats（充值统计表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| product_id | BIGINT | NULL | 产品ID |
| product_name | VARCHAR(255) | NULL | 产品名称 |
| customer_id | BIGINT | NULL | 客户ID |
| customer_name | VARCHAR(255) | NULL | 客户名称 |
| supplier_id | BIGINT | NULL | 供应商ID |
| supplier_name | VARCHAR(255) | NULL | 供应商名称 |
| status | VARCHAR(20) | NULL | 充值状态 |
| product_face_price | DECIMAL(10,2) | NULL | 产品面值 |
| supplier_price | DECIMAL(10,2) | NULL | 成本单价 |
| customer_price | DECIMAL(10,2) | NULL | 售卖单价 |
| recharge_count | INT | NULL | 充值数量 |
| cost_amount | DECIMAL(10,2) | NULL | 成本金额 |
| customer_amount | DECIMAL(10,2) | NULL | 销售金额 |
| stat_date | DATE | NULL | 统计日期 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

### 18. messages（站内信表）
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | 用户ID |
| title | VARCHAR(255) | NULL | 消息标题 |
| content | TEXT | NULL | 消息内容 |
| is_read | BIT(1) | NOT NULL | 是否已读 |
| created_at | DATETIME(6) | NOT NULL | 创建时间 |
| updated_at | DATETIME(6) | NOT NULL | 更新时间 |

## API接口列表

### 认证接口
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/auth/signin` | POST | 用户登录 |
| `/api/auth/signup` | POST | 用户注册 |

### 用户管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/users` | GET | 获取用户列表 |
| `/api/users/{id}` | GET | 获取用户详情 |
| `/api/users` | POST | 创建用户 |
| `/api/users/{id}` | PUT | 更新用户 |
| `/api/users/{id}` | DELETE | 删除用户 |

### 角色管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/roles` | GET | 获取角色列表 |
| `/api/roles/{id}` | GET | 获取角色详情 |
| `/api/roles` | POST | 创建角色 |
| `/api/roles/{id}` | PUT | 更新角色 |
| `/api/roles/{id}` | DELETE | 删除角色 |

### 权限管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/permissions` | GET | 获取权限列表 |
| `/api/permissions/{id}` | GET | 获取权限详情 |
| `/api/permissions` | POST | 创建权限 |
| `/api/permissions/{id}` | PUT | 更新权限 |
| `/api/permissions/{id}` | DELETE | 删除权限 |

### 产品管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/products` | GET | 获取产品列表 |
| `/api/products/{id}` | GET | 获取产品详情 |
| `/api/products` | POST | 创建产品 |
| `/api/products/{id}` | PUT | 更新产品 |

### 供应商管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/suppliers` | GET | 获取供应商列表 |
| `/api/suppliers/{id}` | GET | 获取供应商详情 |
| `/api/suppliers` | POST | 创建供应商 |
| `/api/suppliers/{id}` | PUT | 更新供应商 |

### 供应商产品管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/supplier-products` | GET | 获取供应商产品列表 |
| `/api/supplier-products/{id}` | GET | 获取供应商产品详情 |
| `/api/supplier-products` | POST | 创建供应商产品 |
| `/api/supplier-products/{id}` | PUT | 更新供应商产品 |

### 供应商余额管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/supplier-balances` | GET | 获取供应商余额列表 |
| `/api/supplier-balances/{id}` | GET | 获取供应商余额详情 |
| `/api/supplier-balances/{id}` | PUT | 更新供应商余额 |

### 供应商充值记录
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/supplier-recharges` | GET | 获取供应商充值记录列表 |
| `/api/supplier-recharges` | POST | 创建供应商充值记录 |

### 客户管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/customers` | GET | 获取客户列表 |
| `/api/customers/{id}` | GET | 获取客户详情 |
| `/api/customers` | POST | 创建客户 |
| `/api/customers/{id}` | PUT | 更新客户 |

### 客户产品管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/customer-products` | GET | 获取客户产品列表 |
| `/api/customer-products/{id}` | GET | 获取客户产品详情 |
| `/api/customer-products` | POST | 创建客户产品 |
| `/api/customer-products/{id}` | PUT | 更新客户产品 |
| `/api/customer-products/{id}` | DELETE | 删除客户产品 |

### 供应商顺序管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/customer-product-supplier-orders` | GET | 获取供应商顺序列表 |
| `/api/customer-product-supplier-orders` | POST | 保存供应商顺序 |
| `/api/customer-product-supplier-orders/{customerProductId}` | DELETE | 删除供应商顺序 |

### 客户余额管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/customer-balances` | GET | 获取客户余额列表 |
| `/api/customer-balances/{id}` | GET | 获取客户余额详情 |
| `/api/customer-balances/{id}` | PUT | 更新客户余额 |

### 客户付款管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/customer-payments` | GET | 获取客户付款列表 |
| `/api/customer-payments` | POST | 创建客户付款 |

### 充值记录管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/recharge-records` | GET | 获取充值记录列表 |
| `/api/recharge-records/{id}` | GET | 获取充值记录详情 |
| `/api/recharge-records` | POST | 创建充值记录 |
| `/api/recharge-records/status/{status}` | GET | 按状态查询 |
| `/api/recharge-records/product/{productName}` | GET | 按产品查询 |

### 统计数据
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/stats` | GET | 获取统计数据 |
| `/api/stats/list` | GET | 获取聚合统计列表 |

### 站内信管理
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/messages` | GET | 获取当前用户消息列表 |
| `/api/messages/{id}` | GET | 获取消息详情 |
| `/api/messages/{id}` | PUT | 标记消息为已读 |
| `/api/messages/{id}` | DELETE | 删除消息 |
| `/api/messages/unread/count` | GET | 获取未读消息数量 |

### 文件上传
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/upload` | POST | 上传文件 |

### 测试数据生成
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/generate/recharge-records` | GET | 生成测试充值记录 |

## 前端页面功能

### 1. 仪表盘 (Dashboard)
- 系统概览数据展示
- 关键指标统计

### 2. 用户管理 (UserManagement)
- 用户列表展示
- 用户增删改查

### 3. 角色管理 (RoleManagement)
- 角色列表展示
- 角色增删改查

### 4. 权限管理 (PermissionManagement)
- 权限列表展示
- 权限增删改查

### 5. 供应商管理 (SupplierManagement)
- 供应商列表展示（含充值总额、余额）
- 供应商产品关联管理
- 供应商产品增删改查

### 6. 供应商产品管理 (SupplierProductManagement)
- 供应商产品列表展示
- 供应商产品修改（供应商编码不可修改）

### 7. 供应商余额管理 (SupplierBalanceManagement)
- 供应商余额列表展示
- 供应商充值记录

### 8. 产品管理 (ProductManagement)
- 产品列表展示（默认倒序）
- 产品增删改（名称、类型不可修改）

### 9. 客户管理 (CustomerManagement)
- 客户列表展示
- 客户产品分配管理

### 10. 客户产品管理 (CustomerProductManagement)
- 客户产品列表展示
- 供应商顺序调整
- 客户价格和库存量修改

### 11. 客户付款管理 (CustomerPaymentManagement)
- 客户付款记录列表

### 12. 客户余额管理 (CustomerBalanceManagement)
- 客户余额列表展示
- 大客户通行状态管理

### 13. 充值记录管理 (RechargeRecordManagement)
- 充值记录列表展示（日期筛选）
- 手动充值功能
- 批量充值功能（Excel导入）
- Excel导出功能

### 14. 统计数据 (StatsManagement)
- 统计数据列表展示
- 多维度筛选
- Excel导出功能

### 15. 每日统计 (DailyStatsManagement)
- 每日统计数据展示

### 16. 站内信管理 (MessageManagement)
- 消息列表展示（倒序）
- 未读消息红点标记
- 消息详情查看
- 消息已读状态管理

## 部署说明

### 环境要求
- Java 1.8+
- Node.js 14+
- MySQL 8.0+

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

服务将在 http://localhost:8080/api 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

服务将在 http://localhost:5175 启动

### 数据库配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/membership_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 日志配置

日志级别可在 `application.yml` 中配置：

```yaml
logging:
  file:
    name: /path/to/log/backend.log
  level:
    root: INFO
    com.membership: DEBUG  # 项目包使用DEBUG级别
```

## 安全说明

- 使用JWT进行身份认证
- 密码采用BCrypt加密存储
- 支持IP白名单限制
- 角色权限控制（基于Spring Security）
- 站内信功能无需权限，每个用户仅能查看自己的消息

## 主要功能特性

1. **产品管理**：产品信息维护，支持禁用/启用状态
2. **供应商管理**：供应商信息维护，产品关联，余额管理
3. **客户管理**：客户信息维护，产品分配，余额管理
4. **充值管理**：支持手动充值和批量充值，记录追踪
5. **统计分析**：多维度统计数据，支持Excel导出
6. **供应商顺序**：支持客户产品的供应商调用顺序配置
7. **大客户通行**：支持大客户快速通行状态设置
8. **站内信系统**：用户消息通知，未读提醒，消息详情
9. **付款账户管理**：客户付款记录自动更新账户总额
10. **授信账户管理**：支持授信额度管理和余额计算

## 余额计算公式

### 客户余额计算逻辑
- **付款账户余额** = 付款账户总额 - 付款账户已消费金额
- **授信账户余额** = 授信账户总额 - 授信账户已消费金额
- **账户总计总额** = 付款账户总额 + 授信账户总额
- **账户总计已消费** = 付款账户已消费金额 + 授信账户已消费金额
- **账户总计余额** = 付款账户余额 + 授信账户余额

### 供应商余额计算逻辑
- **剩余金额** = 充值总额 - 已消费金额

## 核心业务流程

### 充值流程
1. 用户选择客户和产品
2. 系统根据供应商顺序选择最优供应商
3. 创建充值记录
4. 更新客户余额（扣除相应金额）
5. 更新供应商余额（扣除成本）
6. 记录统计数据

### 付款流程
1. 创建付款记录（付款金额/授信金额）
2. 自动更新客户付款账户总额
3. 重新计算客户余额

## 开发注意事项

1. 后端使用Spring Boot标准分层架构
2. 前端使用React + Ant Design组件库
3. 数据库使用JPA自动建表（ddl-auto: update）
4. 定时任务自动统计每日充值数据
5. 日志使用SLF4J框架，所有Controller已添加详细日志
6. 使用Java 8语法，避免使用Java 9+特性

## 数据库备份与恢复

### 备份数据库
```bash
mysqldump -u username -p --no-tablespaces membership_db > init-database.sql
```

### 恢复数据库
```bash
mysql -u username -p membership_db < init-database.sql
```

### 当前备份文件
- `init-database.sql` - 初始数据库脚本
- `init-database-20260519.sql` - 2026年5月19日数据库备份

## 工具类说明

### MessageService
用于发送站内信的工具类，提供以下方法：
- `sendMessage(Long userId, String title, String content)` - 向指定用户发送消息
- `sendMessageToAll(String title, String content)` - 向所有用户发送消息
- `sendMessages(List<Long> userIds, String title, String content)` - 向多个用户发送消息

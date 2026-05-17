# 会员管理系统 (Membership Management System)

## 项目概述

本系统是一个会员管理系统，主要用于管理客户、供应商、产品以及充值记录等业务，提供完整的统计分析功能。

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
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层
│   │   ├── repository/         # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # 安全相关
│   │   └── scheduler/          # 定时任务
│   ├── src/main/resources/
│   │   └── application.yml     # 应用配置
│   └── pom.xml                 # Maven配置
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── pages/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── utils/              # 工具函数
│   │   └── contexts/           # 状态管理
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md                   # 项目说明文档
```

## 数据库表结构

### 1. users（用户表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码（加密） |
| email | VARCHAR(100) | 邮箱 |
| enabled | BOOLEAN | 是否启用 |

### 2. roles（角色表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 角色名称 |

### 3. permissions（权限表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 权限名称 |
| code | VARCHAR(50) | 权限编码 |

### 4. products（产品表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 产品名称 |
| type | VARCHAR(50) | 产品类型 |
| face_value | DECIMAL(10,2) | 面值 |
| description | VARCHAR(500) | 描述 |
| enabled | BOOLEAN | 是否启用 |

### 5. suppliers（供应商表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 供应商名称 |
| supplier_code | VARCHAR(16) | 供应商编码（唯一） |
| cooperation_start_date | DATETIME | 合作开始日期 |
| enabled | BOOLEAN | 是否启用 |

### 6. customers（客户表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 客户名称 |
| customer_code | VARCHAR(16) | 客户编码（唯一） |
| customer_secret | VARCHAR(16) | 客户秘钥 |
| ip_whitelist | TEXT | IP白名单 |
| cooperation_start_date | DATETIME | 合作开始日期 |
| enabled | BOOLEAN | 是否启用 |

### 7. supplier_products（供应商产品表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| product_id | BIGINT | 产品ID |
| supplier_id | BIGINT | 供应商ID |
| supplier_price | DECIMAL(10,2) | 供应商价格 |
| stock_quantity | INT | 库存量 |
| sales_quantity | INT | 出货量 |
| stock_amount | DECIMAL(10,2) | 库存金额 |
| sales_amount | DECIMAL(10,2) | 出货金额 |
| enabled | BOOLEAN | 是否启用 |
| supplier_product_code | VARCHAR(16) | 供应商产品码 |
| face_value | DECIMAL(10,2) | 面值 |
| daily_stock_limit | INT | 每日库存限量 |

### 8. customer_products（客户产品表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| customer_id | BIGINT | 客户ID |
| product_id | BIGINT | 产品ID |
| customer_product_code | VARCHAR(32) | 客户产品码（唯一） |
| customer_price | DECIMAL(10,2) | 客户价格 |
| stock_quantity | INT | 库存量 |
| shipment_quantity | INT | 出货量 |
| stock_amount | DECIMAL(10,2) | 库存金额 |
| shipment_amount | DECIMAL(10,2) | 出货金额 |
| enabled | BOOLEAN | 是否启用 |

### 9. customer_product_supplier_orders（客户产品供应商顺序表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| customer_product_id | BIGINT | 客户产品ID |
| supplier_id | BIGINT | 供应商ID |
| order_index | INT | 顺序索引 |

### 10. customer_balances（客户余额表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| customer_id | BIGINT | 客户ID |
| payment_account_total | DECIMAL(10,2) | 付款账户总额 |
| payment_account_consumed | DECIMAL(10,2) | 付款账户已消费 |
| payment_account_balance | DECIMAL(10,2) | 付款账户余额 |
| credit_account_total | DECIMAL(10,2) | 授信账户总额 |
| credit_account_consumed | DECIMAL(10,2) | 授信账户已消费 |
| credit_account_balance | DECIMAL(10,2) | 授信账户余额 |
| total_account_amount | DECIMAL(10,2) | 账户总额 |
| total_account_consumed | DECIMAL(10,2) | 账户总消费 |
| total_account_balance | DECIMAL(10,2) | 账户总余额 |
| alert_threshold | DECIMAL(10,2) | 预警阈值 |
| enabled | BOOLEAN | 是否启用 |
| vip_pass_enabled | BOOLEAN | 大客户通行状态 |

### 11. supplier_balances（供应商余额表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| supplier_id | BIGINT | 供应商ID |
| recharge_total | DECIMAL(10,2) | 充值总额 |
| consumed_total | DECIMAL(10,2) | 消费总额 |
| balance | DECIMAL(10,2) | 余额 |

### 12. recharge_records（充值记录表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| customer_product_id | BIGINT | 客户产品ID |
| product_id | BIGINT | 产品ID |
| product_name | VARCHAR(100) | 产品名称 |
| recharge_phone | VARCHAR(20) | 充值手机号 |
| customer_order_id | VARCHAR(50) | 客户订单ID |
| platform_order_id | VARCHAR(50) | 平台订单ID |
| customer_id | BIGINT | 客户ID |
| customer_name | VARCHAR(100) | 客户名称 |
| customer_price | DECIMAL(10,2) | 客户价格 |
| supplier_id | BIGINT | 供应商ID |
| supplier_name | VARCHAR(100) | 供应商名称 |
| supplier_price | DECIMAL(10,2) | 供应商价格 |
| product_face_price | DECIMAL(10,2) | 产品票面价格 |
| status | VARCHAR(20) | 状态 |
| description | VARCHAR(500) | 描述 |
| recharge_person | BIGINT | 充值人ID |

### 13. recharge_stats（充值统计表）
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | BIGINT | 主键 |
| product_id | BIGINT | 产品ID |
| product_name | VARCHAR(100) | 产品名称 |
| customer_id | BIGINT | 客户ID |
| customer_name | VARCHAR(100) | 客户名称 |
| supplier_id | BIGINT | 供应商ID |
| supplier_name | VARCHAR(100) | 供应商名称 |
| status | VARCHAR(20) | 充值状态 |
| product_face_price | DECIMAL(10,2) | 产品面值 |
| supplier_price | DECIMAL(10,2) | 成本单价 |
| customer_price | DECIMAL(10,2) | 售卖单价 |
| recharge_count | INT | 充值数量 |
| cost_amount | DECIMAL(10,2) | 成本金额 |
| customer_amount | DECIMAL(10,2) | 销售金额 |
| stat_date | DATE | 统计日期 |

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

### 统计数据
| API路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/stats` | GET | 获取统计数据 |

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

## 安全说明

- 使用JWT进行身份认证
- 密码采用BCrypt加密存储
- 支持IP白名单限制
- 角色权限控制

## 主要功能特性

1. **产品管理**：产品信息维护，支持禁用/启用状态
2. **供应商管理**：供应商信息维护，产品关联，余额管理
3. **客户管理**：客户信息维护，产品分配，余额管理
4. **充值管理**：支持手动充值和批量充值，记录追踪
5. **统计分析**：多维度统计数据，支持Excel导出
6. **供应商顺序**：支持客户产品的供应商调用顺序配置
7. **大客户通行**：支持大客户快速通行状态设置

## 开发注意事项

1. 后端使用Spring Boot标准分层架构
2. 前端使用React + Ant Design组件库
3. 数据库使用JPA自动建表（ddl-auto: update）
4. 定时任务自动统计每日充值数据

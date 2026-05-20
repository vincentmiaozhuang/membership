# 会员管理系统 (Membership Management System)

## 项目概述

本系统是一个会员管理系统，主要用于管理客户、供应商、产品以及充值记录等业务，提供完整的统计分析功能和站内信通知功能。

**架构设计**：采用 Maven 多模块架构，实现内部管理后台与对外接口的物理隔离。

---

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Security | 5.7.x | 安全框架 |
| Spring Data JPA | 2.7.x | 数据访问层 |
| MySQL | 8.0+ | 数据库 |
| JWT | 0.12.3 | 身份认证（内部系统） |
| Bucket4j | 8.7.0 | 限流（对外接口） |
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

---

## 项目结构

```
membership-system/
├── pom.xml                          # 父项目POM
├── membership-core/                 # 核心模块（共享）
│   ├── pom.xml
│   └── src/main/java/com/membership/core/
│       ├── entity/                  # 实体类（15个）
│       ├── repository/              # 数据访问层（19个）
│       ├── service/                 # 核心业务服务
│       └── config/                  # 核心配置
├── membership-internal/             # 内部管理后台
│   ├── pom.xml
│   └── src/main/java/com/membership/internal/
│       ├── controller/              # 内部API控制器（19个）
│       ├── dto/                     # 内部数据传输对象
│       ├── config/                  # 内部配置
│       ├── security/                # Spring Security配置
│       ├── scheduler/               # 定时任务
│       └── InternalApplication.java # 启动类
├── membership-external/             # 对外接口模块
│   ├── pom.xml
│   └── src/main/java/com/membership/external/
│       ├── controller/              # 对外API控制器
│       ├── dto/                     # 对外数据传输对象
│       ├── config/                  # 对外配置（API Key、限流等）
│       └── ExternalApplication.java # 启动类
├── frontend/                        # 前端应用
│   ├── src/
│   │   ├── pages/                   # 页面组件（15个页面）
│   │   ├── components/              # 通用组件
│   │   ├── utils/                   # 工具函数
│   │   └── contexts/                # 状态管理
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── log/                             # 日志目录
├── scripts/                         # 部署脚本
│   ├── start-internal.sh
│   └── start-external.sh
├── init-database.sql                # 数据库初始化脚本
├── init-database-20260519.sql       # 数据库备份 (2026-05-19)
└── README.md                        # 项目说明文档
```

---

## 模块职责说明

### 1. membership-core（核心模块）
- **职责**：提供共享的实体类、数据访问层和核心业务逻辑
- **特点**：无状态、可复用、不包含任何对外暴露的API
- **包含内容**：
  - `entity/`：所有数据库实体类
  - `repository/`：所有数据访问接口
  - `service/`：核心业务服务（如消息服务）
  - `config/`：JPA配置等核心配置

### 2. membership-internal（内部管理后台）
- **职责**：供内部管理人员使用的管理后台
- **认证方式**：JWT Token + Spring Security
- **访问路径**：`/api/**`
- **端口**：8080
- **特点**：完整的权限控制、细粒度角色权限管理

### 3. membership-external（对外接口）
- **职责**：供外部系统调用的公开API
- **认证方式**：API Key
- **访问路径**：`/api/external/**`
- **端口**：8081
- **特点**：限流保护、独立数据库用户权限、脱敏响应

---

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

### 表清单（18张表）
| 序号 | 表名 | 说明 |
| :--- | :--- | :--- |
| 1 | users | 用户表 |
| 2 | roles | 角色表 |
| 3 | permissions | 权限表 |
| 4 | user_roles | 用户角色关联表 |
| 5 | role_permissions | 角色权限关联表 |
| 6 | products | 产品表 |
| 7 | suppliers | 供应商表 |
| 8 | customers | 客户表 |
| 9 | supplier_products | 供应商产品表 |
| 10 | customer_products | 客户产品表 |
| 11 | customer_product_supplier_orders | 客户产品供应商顺序表 |
| 12 | customer_balances | 客户余额表 |
| 13 | customer_payments | 客户付款记录表 |
| 14 | supplier_balances | 供应商余额表 |
| 15 | supplier_recharges | 供应商充值记录表 |
| 16 | recharge_records | 充值记录表 |
| 17 | recharge_stats | 充值统计表 |
| 18 | messages | 站内信表 |

---

## 余额计算公式

### 客户余额计算逻辑
- **付款账户余额** = 付款账户总额 - 付款账户已消费金额
- **授信账户余额** = 授信账户总额 - 授信账户已消费金额
- **账户总计总额** = 付款账户总额 + 授信账户总额
- **账户总计已消费** = 付款账户已消费金额 + 授信账户已消费金额
- **账户总计余额** = 付款账户余额 + 授信账户余额

### 供应商余额计算逻辑
- **剩余金额** = 充值总额 - 已消费金额

---

## 核心业务流程

### 充值流程
```
客户下单 → 验证客户余额 → 选择供应商 → 创建充值记录 → 更新客户余额 → 更新供应商余额 → 发送站内信通知
```

### 付款流程
```
客户付款 → 创建付款记录 → 更新付款账户总额 → 重新计算账户余额
```

---

## 部署指南

### 前置条件
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 1. 数据库配置

创建数据库和用户：
```sql
-- 创建数据库
CREATE DATABASE membership_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建内部管理后台用户（完整权限）
CREATE USER 'internal_user'@'%' IDENTIFIED BY 'Internal@123';
GRANT ALL PRIVILEGES ON membership_db.* TO 'internal_user'@'%';

-- 创建对外接口用户（只读权限）
CREATE USER 'external_user'@'%' IDENTIFIED BY 'External@123';
GRANT SELECT ON membership_db.customers TO 'external_user'@'%';
GRANT SELECT ON membership_db.products TO 'external_user'@'%';
GRANT SELECT ON membership_db.customer_balances TO 'external_user'@'%';
GRANT INSERT ON membership_db.recharge_records TO 'external_user'@'%';

FLUSH PRIVILEGES;
```

### 2. 初始化数据库
```bash
mysql -u internal_user -pInternal@123 membership_db < init-database.sql
```

### 3. 编译打包

```bash
# 进入项目根目录
cd membership-system

# 编译所有模块
mvn clean package -DskipTests

# 或者只编译特定模块
mvn clean package -DskipTests -pl membership-core
mvn clean package -DskipTests -pl membership-internal
mvn clean package -DskipTests -pl membership-external
```

### 4. 启动服务

#### 方式一：使用脚本启动（推荐）
```bash
# 启动内部管理后台
./scripts/start-internal.sh

# 启动对外接口
./scripts/start-external.sh
```

#### 方式二：直接启动
```bash
# 启动内部管理后台（端口8080）
java -jar membership-internal/target/membership-internal-1.0.0.jar

# 启动对外接口（端口8081）
java -jar membership-external/target/membership-external-1.0.0.jar
```

### 5. 访问地址

| 服务 | 地址 | 说明 |
| :--- | :--- | :--- |
| 内部管理后台 | http://localhost:8080 | 管理后台API |
| 对外接口 | http://localhost:8081/api/external/v1 | 公开API |
| 前端页面 | http://localhost:5173 | React前端 |

---

## API接口说明

### 内部API（管理后台）

| 模块 | 路径 | 说明 |
| :--- | :--- | :--- |
| 认证 | `/api/auth/**` | 登录、注册 |
| 用户管理 | `/api/users/**` | 用户CRUD |
| 角色管理 | `/api/roles/**` | 角色CRUD |
| 权限管理 | `/api/permissions/**` | 权限CRUD |
| 客户管理 | `/api/customers/**` | 客户CRUD |
| 供应商管理 | `/api/suppliers/**` | 供应商CRUD |
| 产品管理 | `/api/products/**` | 产品CRUD |
| 充值记录 | `/api/recharge-records/**` | 充值记录CRUD |
| 站内信 | `/api/messages/**` | 站内信管理 |
| 统计分析 | `/api/stats/**` | 数据统计 |

### 对外API

| 模块 | 路径 | 说明 |
| :--- | :--- | :--- |
| 客户查询 | `/api/external/v1/customers` | 查询客户信息 |
| 客户详情 | `/api/external/v1/customers/{id}` | 查询单个客户 |

**认证方式**：请求头添加 `X-API-Key`

**示例请求**：
```bash
curl -H "X-API-Key: external-api-key-123456" \
     http://localhost:8081/api/external/v1/customers/1
```

---

## 日志配置

### 内部管理后台日志
- 日志文件：`log/internal-backend.log`
- 日志级别：DEBUG（项目包）、INFO（全局）

### 对外接口日志
- 日志文件：`log/external-backend.log`
- 日志级别：DEBUG（项目包）、INFO（全局）

---

## 默认管理员账号

| 用户名 | 密码 | 角色 |
| :--- | :--- | :--- |
| admin | admin123 | 管理员 |

---

## 数据库备份与恢复

### 备份
```bash
mysqldump -u internal_user -pInternal@123 --no-tablespaces membership_db > backup.sql
```

### 恢复
```bash
mysql -u internal_user -pInternal@123 membership_db < backup.sql
```

---

## 安全注意事项

1. **API Key管理**：对外接口使用API Key认证，定期轮换
2. **数据库权限隔离**：对外接口使用只读权限用户
3. **限流保护**：对外接口配置限流策略（100次/分钟/IP）
4. **HTTPS**：生产环境强制使用HTTPS
5. **输入校验**：所有接口进行严格的参数校验
6. **日志脱敏**：敏感信息不在日志中输出

---

## 开发规范

### 代码风格
- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 方法命名采用驼峰命名法

### 日志规范
- 所有Controller添加日志记录
- 记录关键参数和返回值
- 使用DEBUG级别记录详细信息

### 异常处理
- 使用统一异常处理
- 返回统一错误格式
- 生产环境不暴露堆栈信息
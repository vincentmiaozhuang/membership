# 会员卡管理系统

一个基于 Spring Boot + React 的会员卡后台管理系统，实现了用户管理、角色权限管理、供应商管理、产品管理和客户管理等功能。

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Maven

### 前端
- React 18
- Ant Design 5
- React Router DOM
- Axios
- Vite

## 功能特性

1. **用户认证**
   - 用户注册、登录
   - JWT Token 认证
   - 权限控制

2. **用户管理**
   - 用户增删改查
   - 角色分配
   - 密码重置

3. **角色管理**
   - 角色增删改查
   - 权限分配

4. **权限管理**
   - 权限增删改查
   - 资源和操作管理

5. **供应商管理**
   - 供应商增删改查
   - 供应商信息维护

6. **产品管理（会员卡）**
   - 产品增删改查
   - 关联供应商
   - 卡号管理

7. **客户管理**
   - 客户增删改查
   - 产品分配
   - 客户产品管理

## 数据库设计

### 用户相关表
- users（用户表）
- roles（角色表）
- permissions（权限表）
- user_roles（用户角色关联表）
- role_permissions（角色权限关联表）

### 业务表
- suppliers（供应商表）
- products（产品/会员卡表）
- customers（客户表）
- customer_products（客户产品关联表）

## 快速开始

### 前置要求

- JDK 17+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库配置

创建数据库：

```sql
CREATE DATABASE membership_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改后端配置文件 [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/membership_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

首次启动会自动创建表结构并初始化默认数据：
- 管理员账号：admin / admin123
- 默认角色：ROLE_ADMIN, ROLE_USER, ROLE_MANAGER
- 默认权限：所有基础权限

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

### 4. 访问系统

打开浏览器访问 http://localhost:5173

使用默认管理员账号登录：
- 用户名：admin
- 密码：admin123

## 项目结构

```
membership-system/
├── backend/                 # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/membership/
│   │       │   ├── config/      # 配置类
│   │       │   ├── controller/  # 控制器
│   │       │   ├── dto/         # 数据传输对象
│   │       │   ├── entity/      # 实体类
│   │       │   ├── repository/  # 数据访问层
│   │       │   └── security/    # 安全配置
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
└── frontend/                # 前端项目
    ├── src/
    │   ├── components/   # 组件
    │   ├── contexts/    # 上下文
    │   ├── pages/       # 页面
    │   ├── utils/       # 工具
    │   ├── App.jsx
    │   └── main.jsx
    ├── index.html
    └── package.json
```

## API 接口

### 认证接口
- POST /api/auth/login - 用户登录
- POST /api/auth/register - 用户注册

### 用户管理
- GET /api/users - 获取用户列表
- POST /api/users - 创建用户
- PUT /api/users/{id} - 更新用户
- DELETE /api/users/{id} - 删除用户
- PUT /api/users/{id}/reset-password - 重置密码

### 角色管理
- GET /api/roles - 获取角色列表
- POST /api/roles - 创建角色
- PUT /api/roles/{id} - 更新角色
- DELETE /api/roles/{id} - 删除角色

### 权限管理
- GET /api/permissions - 获取权限列表
- POST /api/permissions - 创建权限
- PUT /api/permissions/{id} - 更新权限
- DELETE /api/permissions/{id} - 删除权限

### 供应商管理
- GET /api/suppliers - 获取供应商列表
- POST /api/suppliers - 创建供应商
- PUT /api/suppliers/{id} - 更新供应商
- DELETE /api/suppliers/{id} - 删除供应商

### 产品管理
- GET /api/products - 获取产品列表
- POST /api/products - 创建产品
- PUT /api/products/{id} - 更新产品
- DELETE /api/products/{id} - 删除产品

### 客户管理
- GET /api/customers - 获取客户列表
- POST /api/customers - 创建客户
- PUT /api/customers/{id} - 更新客户
- DELETE /api/customers/{id} - 删除客户
- GET /api/customers/{id}/products - 获取客户产品
- POST /api/customers/{id}/products - 分配产品给客户
- DELETE /api/customers/{id}/products/{productId} - 移除客户产品

## 许可证

MIT License

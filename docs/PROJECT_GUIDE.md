# ChainPass 基于区块链的跨境数字身份与合规支付解决方案 v2.0

## 📋 项目概述

ChainPass 是一个基于区块链技术的企业级跨境数字身份与合规支付解决方案，支持：

- 🆔 **DID去中心化身份** - W3C标准，用户自主控制身份
- 📜 **可验证凭证(VC)** - 身份属性的密码学证明
- 💳 **跨境支付** - 多币种支持，模拟支付流程
- ✅ **KYC认证** - 身份认证与凭证自动签发
- 🔐 **账密登录** - 传统用户名密码登录
- 🔑 **ZKP零知识证明** - Ed25519 签名验证
- 🚀 **OAuth2授权** - Gitee 第三方登录

---

## 🔄 核心功能对比

| 功能模块 | v1.0 | v2.0 | 说明 |
|----------|------|------|------|
| **DID系统** | ❌ | ✅ | W3C标准去中心化身份 |
| **可验证凭证** | ❌ | ✅ | 签发/验证/吊销 |
| **支付系统** | ❌ | ✅ | 模拟跨境支付 |
| **KYC认证** | ❌ | ✅ | 身份认证+VC签发 |
| **用户管理** | ✅ | ✅ | 完整CRUD |
| **权限系统** | ✅ | ✅ | RBAC权限体系 |
| **登录认证** | ✅ | ✅ | 多种登录方式 |

---

## 📁 项目结构

```
chainpass/
├── apps/
│   ├── web/                          # Vue 3 前端
│   │   ├── src/
│   │   │   ├── api/                  # API请求模块
│   │   │   │   ├── auth.ts           # 认证API
│   │   │   │   ├── user.ts           # 用户API
│   │   │   │   ├── oauth.ts          # OAuth API
│   │   │   │   └── zkp.ts            # ZKP API
│   │   │   ├── components/
│   │   │   │   ├── common/           # 通用组件
│   │   │   │   ├── business/         # 业务组件
│   │   │   │   └── layout/           # 布局组件
│   │   │   ├── composables/          # 组合式函数
│   │   │   ├── router/               # 路由配置
│   │   │   ├── stores/               # Pinia状态
│   │   │   ├── styles/               # 全局样式
│   │   │   └── views/                # 页面视图
│   │   ├── public/                   # 静态资源
│   │   ├── package.json
│   │   ├── vite.config.ts
│   │   └── Dockerfile
│   │
│   ├── server/                       # Spring Boot 后端
│   │   ├── src/main/java/com/chainpass/
│   │   │   ├── config/               # 配置类
│   │   │   ├── controller/           # 控制器
│   │   │   ├── service/              # 服务层
│   │   │   ├── mapper/               # MyBatis Mapper
│   │   │   ├── entity/               # 实体类
│   │   │   ├── dto/                  # 数据传输对象
│   │   │   ├── vo/                   # 视图对象
│   │   │   ├── filter/               # 过滤器
│   │   │   ├── handler/              # 异常处理
│   │   │   ├── aspect/               # AOP切面
│   │   │   ├── annotation/           # 自定义注解
│   │   │   └── util/                 # 工具类
│   │   ├── src/main/resources/
│   │   │   ├── application.yml       # 主配置
│   │   │   ├── application-dev.yml   # 开发配置
│   │   │   ├── application-prod.yml  # 生产配置
│   │   │   └── mapper/               # MyBatis XML
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   └── shared/                       # 共享类型
│       └── types/index.ts
│
├── docker/
│   ├── docker-compose.yml            # 开发环境编排
│   └── mysql/init.sql                # 数据库初始化
│
├── .github/workflows/
│   └── ci.yml                        # CI流水线
│
├── docs/                             # 文档目录
├── scripts/                          # 脚本目录
├── package.json                      # Monorepo根配置
├── pnpm-workspace.yaml               # pnpm工作空间
└── README.md
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | >= 20.19.0 | 前端运行环境 |
| pnpm | >= 9.0.0 | 包管理器 |
| Java | 17+ | 后端运行环境 |
| Maven | 3.8+ | Java构建工具 |
| Docker | 20.10+ | 容器化部署 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |

### 第一步：克隆项目

```bash
# 项目位于 KJZF/chainpass 目录
cd chainpass
```

### 第二步：启动基础设施

```bash
# 启动 MySQL 和 Redis
docker-compose -f docker/docker-compose.yml up -d

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 预期输出：
# chainpass-mysql   running   0.0.0.0:3306->3306/tcp
# chainpass-redis   running   0.0.0.0:6379->6379/tcp
```

### 第三步：配置环境变量

创建 `.env` 文件：

```bash
# 在项目根目录创建 .env 文件
cat > .env << 'EOF'
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/chainpass?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=chainpass123

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT配置（生产环境请使用强密钥）
JWT_SECRET=your-super-secret-key-at-least-32-characters-long

# Gitee OAuth配置
GITEE_CLIENT_ID=your_gitee_client_id
GITEE_CLIENT_SECRET=your_gitee_client_secret
GITEE_REDIRECT_URI=http://localhost:8080/oauth/gitee/callback

# 运行环境
SPRING_PROFILES_ACTIVE=dev
EOF
```

### 第四步：启动后端

```bash
# 方式一：Maven 启动（开发环境）
cd apps/server
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/chainpass-server-2.0.0.jar

# 后端启动成功后访问：
# http://localhost:8080/actuator/health
# 预期返回：{"status":"UP"}
```

### 第五步：启动前端

```bash
# 新开一个终端
cd apps/web

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 前端启动成功后访问：
# http://localhost:5173
```

### 第六步：登录系统

访问 http://localhost:5173/auth/login

**默认管理员账号：**
- 用户名：`admin`
- 密码：`admin123`

---

## 🐳 Docker 部署

### 一键部署所有服务

```bash
# 构建镜像
docker-compose -f docker/docker-compose.yml build

# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 查看日志
docker-compose -f docker/docker-compose.yml logs -f
```

### 单独构建镜像

```bash
# 构建前端镜像
cd apps/web
docker build -t chainpass-web:latest .

# 构建后端镜像
cd apps/server
docker build -t chainpass-server:latest .
```

---

## 📡 API 接口文档

### 认证相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/auth/login` | POST | 用户登录 |
| `/auth/logout` | POST | 用户登出 |
| `/auth/refresh` | POST | 刷新Token |
| `/auth/register` | POST | 用户注册 |

### OAuth相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/oauth/gitee/config` | POST | 获取Gitee配置 |
| `/oauth/gitee/callback` | GET | Gitee回调 |

### ZKP认证

| 接口 | 方法 | 描述 |
|------|------|------|
| `/zkp/init` | POST | 初始化认证 |
| `/zkp/public-key` | POST | 提交公钥 |
| `/zkp/verify` | POST | 验证认证 |

### 用户管理

| 接口 | 方法 | 描述 |
|------|------|------|
| `/user/list` | GET | 用户列表 |
| `/user/{id}` | GET | 用户详情 |
| `/user` | POST | 创建用户 |
| `/user/{id}` | PUT | 更新用户 |
| `/user/{id}` | DELETE | 删除用户 |
| `/user/{id}/status` | PUT | 修改状态 |
| `/user/{id}/reset-password` | POST | 重置密码 |

### DID去中心化身份

| 接口 | 方法 | 描述 |
|------|------|------|
| `/did/create` | POST | 创建DID |
| `/did/my` | GET | 获取我的DID |
| `/did/{did}` | GET | 根据DID获取文档 |
| `/did/verify` | POST | 验证DID签名 |
| `/did/revoke` | POST | 吊销DID |
| `/did/check/{did}` | GET | 检查DID有效性 |

### VC可验证凭证

| 接口 | 方法 | 描述 |
|------|------|------|
| `/vc/issue` | POST | 签发凭证 |
| `/vc/verify` | POST | 验证凭证 |
| `/vc/my` | GET | 获取我的凭证 |
| `/vc/list/{did}` | GET | 获取DID的凭证 |
| `/vc/revoke/{vcId}` | POST | 吊销凭证 |
| `/vc/types` | GET | 获取凭证类型 |

### 支付系统

| 接口 | 方法 | 描述 |
|------|------|------|
| `/payment/wallet` | GET | 获取钱包 |
| `/payment/create` | POST | 创建支付订单 |
| `/payment/execute/{orderNo}` | POST | 执行支付 |
| `/payment/history` | GET | 交易历史 |
| `/payment/rate/{from}/{to}` | GET | 获取汇率 |

### KYC认证

| 接口 | 方法 | 描述 |
|------|------|------|
| `/kyc/submit` | POST | 提交KYC认证 |
| `/kyc/status` | GET | 获取KYC状态 |
| `/kyc/detail` | GET | 获取KYC详情 |

---

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

chainpass:
  jwt:
    secret: ${JWT_SECRET}
    access-token-ttl: 900000      # 15分钟
    refresh-token-ttl: 604800000  # 7天

  oauth2:
    gitee:
      client-id: ${GITEE_CLIENT_ID}
      client-secret: ${GITEE_CLIENT_SECRET}
      redirect-uri: ${GITEE_REDIRECT_URI}
```

### 前端配置 (.env)

```env
VITE_API_BASE_URL=/api
VITE_APP_TITLE=ChainPass 区块链身份验证系统
```

---

## 🔐 安全说明

### Token机制

```
┌─────────────────────────────────────────────────────────┐
│                    双Token机制                           │
├─────────────────────────────────────────────────────────┤
│  AccessToken (15分钟)                                    │
│  ├── 用于日常API请求                                      │
│  └── 存储于 Pinia + localStorage                        │
│                                                          │
│  RefreshToken (7天)                                      │
│  ├── 用于刷新AccessToken                                 │
│  └── 存储于 Redis + localStorage                        │
└─────────────────────────────────────────────────────────┘
```

### 认证流程

```
1. 用户登录 → 获取 AccessToken + RefreshToken
2. API请求 → 携带 AccessToken
3. Token过期 → 自动使用 RefreshToken 刷新
4. 刷新失败 → 跳转登录页
```

### 生产环境检查清单

- [ ] 修改 JWT_SECRET 为强密钥
- [ ] 修改数据库密码
- [ ] 配置 HTTPS
- [ ] 配置跨域白名单
- [ ] 配置 Redis 密码
- [ ] 启用请求日志
- [ ] 配置限流策略
- [ ] 配置 Gitee OAuth

### 安全配置详细指南

| 配置项 | 文档 |
|--------|------|
| JWT密钥 | `docs/SECURITY_GUIDE.md` |
| 数据库密码 | `docs/SECURITY_GUIDE.md` |
| HTTPS/SSL | `docs/SECURITY_GUIDE.md` + `scripts/generate-ssl.sh` |
| OAuth配置 | `docs/OAUTH_GUIDE.md` |
| 环境变量 | `.env.example` + `.env.production.example` |

**一键配置脚本：**
```bash
# 生成所有安全密钥
cd chainpass/scripts
./generate-secrets.sh

# 生成SSL证书（可选）
./generate-ssl.sh
```

---

## 📊 数据库表结构

### 核心表

| 表名 | 描述 |
|------|------|
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_permission` | 权限表 |
| `sys_user_role` | 用户角色关联 |
| `sys_role_permission` | 角色权限关联 |
| `sys_login_log` | 登录日志 |
| `sys_operation_log` | 操作日志 |

### 初始化脚本

数据库会在 Docker 启动时自动初始化，脚本位于：
`docker/mysql/init.sql`

---

## 🛠️ 开发指南

### 前端开发

```bash
cd apps/web

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 类型检查
pnpm type-check

# 构建生产版本
pnpm build

# 预览构建结果
pnpm preview
```

### 后端开发

```bash
cd apps/server

# 编译
mvn compile

# 运行测试
mvn test

# 打包
mvn package

# 运行
mvn spring-boot:run
```

### 添加新功能

1. **后端**：
   - 在 `entity/` 创建实体类
   - 在 `mapper/` 创建 Mapper 接口
   - 在 `service/` 创建服务类
   - 在 `controller/` 创建控制器

2. **前端**：
   - 在 `views/` 创建页面组件
   - 在 `api/` 创建 API 模块
   - 在 `router/index.ts` 添加路由
   - 在 `stores/modules/` 添加状态（可选）

---

## 📝 常见问题

### Q: 后端启动失败，数据库连接错误？
```bash
# 检查 MySQL 是否运行
docker ps | grep mysql

# 检查数据库是否创建
docker exec -it chainpass-mysql mysql -uroot -pchainpass123 -e "SHOW DATABASES;"
```

### Q: 前端请求后端 401 错误？
```
检查 Token 是否正确携带：
1. 确保已登录
2. 检查 localStorage 中的 token
3. 检查请求头 Authorization: Bearer {token}
```

### Q: OAuth 登录失败？
```
1. 检查 Gitee OAuth 应用配置
2. 确认回调地址与配置一致
3. 检查 client_id 和 client_secret
```

### Q: 如何重置管理员密码？
```sql
-- 连接数据库
docker exec -it chainpass-mysql mysql -uroot -pchainpass123 chainpass

-- 重置密码为 admin123
UPDATE sys_user
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'
WHERE username = 'admin';
```

---

## 📞 技术支持

- **项目地址**：`KJZF/chainpass`
- **文档更新**：2024年
- **技术栈**：Vue 3 + Spring Boot 3 + MySQL 8 + Redis 7

---

## 📄 License

MIT License

Copyright (c) 2024 ChainPass Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
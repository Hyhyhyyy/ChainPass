# ChainPass - 基于区块链的跨境数字身份与合规支付解决方案

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

**W3C DID标准 | 可验证凭证 | 跨境支付 | 合规认证**

[在线演示](#) | [API文档](#) | [使用指南](docs/PROJECT_GUIDE.md) | [大创材料](docs/INNOVATION_PROJECT.md)

</div>

---

## 🎯 项目简介

ChainPass 是一个基于区块链技术的跨境数字身份与合规支付解决方案，为大创项目"基于区块链的跨境数字身份与合规支付解决方案"的核心实现。

### 核心功能

| 模块 | 功能 | 状态 |
|------|------|------|
| 🆔 **DID系统** | 去中心化身份创建、验证、管理 | ✅ 已完成 |
| 📜 **可验证凭证** | VC签发、验证、吊销 | ✅ 已完成 |
| 💳 **跨境支付** | 多币种钱包、模拟支付、汇率转换 | ✅ 已完成 |
| ✅ **KYC认证** | 身份认证、自动审批、VC发放 | ✅ 已完成 |
| 🔐 **多种登录** | 账密、OAuth、ZKP零知识证明 | ✅ 已完成 |

---

## 📊 系统架构

```
┌────────────────────────────────────────────────────────────────┐
│                        用户界面层                               │
│         Vue 3 Web App  │  Mobile App (预留)  │  开放API        │
├────────────────────────────────────────────────────────────────┤
│                        服务层                                   │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │
│  │ DID服务   │ │ VC服务    │ │ 支付服务  │ │ KYC服务   │      │
│  │ 创建/验证 │ │ 签发/验证 │ │ 钱包/跨境 │ │ 认证/审核 │      │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘      │
├────────────────────────────────────────────────────────────────┤
│                        数据层                                   │
│      MySQL 8.0     │     Redis 7.0     │    模拟区块链        │
└────────────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | JDK 17 LTS |
| Node.js | 20+ | 前端运行环境 |
| pnpm | 9+ | 包管理器 |
| Docker | 20+ | 容器化部署 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |

### 一键启动

```bash
# 1. 克隆项目
git clone [repository-url]
cd chainpass

# 2. 启动基础设施（MySQL + Redis）
docker-compose -f docker/docker-compose.yml up -d

# 3. 初始化数据库
mysql -u root -p < docker/mysql/init-v2.sql

# 4. 启动后端
cd apps/server
mvn spring-boot:run

# 5. 启动前端（新终端）
cd apps/web
pnpm install
pnpm dev
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端应用 | http://localhost:5173 |
| 后端API | http://localhost:8080 |
| Swagger文档 | http://localhost:8080/swagger-ui.html |
| 默认账号 | admin / admin123 |

---

## 📡 API 概览

### DID 接口

```bash
# 创建DID
POST /did/create

# 验证DID签名
POST /did/verify

# 获取我的DID
GET /did/my
```

### VC 接口

```bash
# 签发凭证
POST /vc/issue

# 验证凭证
POST /vc/verify

# 获取凭证列表
GET /vc/my
```

### 支付接口

```bash
# 获取钱包
GET /payment/wallet

# 创建支付订单
POST /payment/create

# 执行支付
POST /payment/execute/{orderNo}
```

### KYC 接口

```bash
# 提交KYC认证
POST /kyc/submit

# 获取认证状态
GET /kyc/status
```

完整API文档请访问 Swagger UI：http://localhost:8080/swagger-ui.html

---

## 📁 项目结构

```
chainpass/
├── apps/
│   ├── web/                          # Vue 3 前端
│   │   ├── src/
│   │   │   ├── api/                  # API请求模块
│   │   │   ├── components/           # 组件
│   │   │   ├── router/               # 路由配置
│   │   │   ├── stores/               # Pinia状态管理
│   │   │   └── views/                # 页面视图
│   │   └── package.json
│   │
│   └── server/                       # Spring Boot 后端
│       ├── src/main/java/com/chainpass/
│       │   ├── did/                  # DID去中心化身份
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   └── entity/
│       │   ├── vc/                   # 可验证凭证
│       │   ├── payment/              # 跨境支付
│       │   ├── compliance/           # 合规系统(KYC)
│       │   ├── config/               # 配置类
│       │   └── util/                 # 工具类
│       └── pom.xml
│
├── docker/
│   ├── docker-compose.yml            # 开发环境编排
│   └── mysql/
│       └── init-v2.sql               # 数据库初始化
│
├── docs/
│   ├── PROJECT_GUIDE.md              # 项目使用指南
│   ├── API_DOCUMENTATION.md          # API文档
│   ├── SECURITY_GUIDE.md             # 安全配置指南
│   ├── INNOVATION_PROJECT.md         # 大创项目材料
│   └── PITCH_DECK_OUTLINE.md         # 路演PPT大纲
│
├── scripts/
│   ├── generate-secrets.sh           # 密钥生成脚本
│   └── generate-ssl.sh               # SSL证书生成
│
└── README.md
```

---

## 🧪 测试

```bash
# 运行后端测试
cd apps/server
mvn test

# 运行前端测试
cd apps/web
pnpm test
```

---

## 🔐 安全配置

生产环境部署请参考 [安全配置指南](docs/SECURITY_GUIDE.md)：

1. **JWT密钥**: 使用强密钥（≥64字符）
2. **数据库密码**: 使用强密码，专用账户
3. **Redis密码**: 生产环境必须设置
4. **HTTPS**: 配置SSL证书
5. **OAuth**: 配置真实的Gitee应用

---

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [项目指南](docs/PROJECT_GUIDE.md) | 完整项目使用说明 |
| [API文档](docs/API_DOCUMENTATION.md) | REST API接口文档 |
| [安全配置](docs/SECURITY_GUIDE.md) | 生产环境安全配置 |
| [大创材料](docs/INNOVATION_PROJECT.md) | 项目计划书、商业分析 |
| [路演大纲](docs/PITCH_DECK_OUTLINE.md) | 答辩PPT结构 |

---

## 🏆 创新点

1. **W3C DID标准**: 采用国际标准的去中心化身份
2. **可验证凭证**: 密码学证明的身份属性，一次认证多平台使用
3. **零知识证明**: Ed25519签名验证，隐私保护
4. **跨境支付**: 多币种支持，汇率转换，合规风控
5. **自动化合规**: KYC认证通过自动签发VC凭证

---

## 📞 联系方式

- **项目地址**: [GitHub链接]
- **问题反馈**: [Issues]
- **邮箱**: team@chainpass.io

---

## 📄 License

MIT License

Copyright (c) 2024 ChainPass Team

---

<div align="center">

**Made with ❤️ by ChainPass Team**

</div>
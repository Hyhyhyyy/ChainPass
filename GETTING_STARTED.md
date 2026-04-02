# ChainPass 项目试用指南

## 🚀 快速开始

### 一、环境准备

#### 必需环境
| 环境 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | >= 20.19.0 或 >= 22.12.0 | 前端运行环境 |
| pnpm | >= 9.0.0 | 包管理器 |
| JDK | 17 | 后端运行环境 |
| Maven | >= 3.8 | Java构建工具 |
| MySQL | 8.0 | 数据库 |
| Redis | >= 7.0 | 缓存服务 |

#### 安装 pnpm（如未安装）
```bash
npm install -g pnpm
```

---

### 二、启动基础设施

#### 方式1: 使用 Docker（推荐）

```bash
cd C:\Users\13581\Desktop\KJZF\chainpass\docker

# 启动 MySQL + Redis
docker-compose up -d
```

#### 方式2: 本地安装

确保本地 MySQL 和 Redis 已启动，并创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS chainpass
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

初始化数据库：
```bash
mysql -u root -p chainpass < docker/mysql/init-v2.sql
```

---

### 三、启动后端服务

```bash
# 进入后端目录
cd C:\Users\13581\Desktop\KJZF\chainpass\apps\server

# 安装依赖
mvn clean install -DskipTests

# 启动开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动成功后访问：
- API地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

---

### 四、启动前端服务

```bash
# 进入项目根目录
cd C:\Users\13581\Desktop\KJZF\chainpass

# 安装依赖
pnpm install

# 启动前端开发服务器
pnpm dev
```

前端启动成功后访问：
- 应用地址: http://localhost:5173

---

## 🧪 功能测试流程

### 第一步：用户注册

1. 访问 http://localhost:5173
2. 点击「注册」按钮
3. 填写注册信息：
   - 用户名: `testuser`
   - 邮箱: `test@example.com`
   - 密码: `Test@123456`
4. 点击「注册」完成注册

### 第二步：登录系统

1. 使用注册的账号登录
2. 进入仪表盘，查看新手引导

### 第三步：创建DID身份

1. 点击侧边栏「身份管理」→「DID身份」
2. 点击「立即创建DID」按钮
3. 确认创建
4. 查看生成的DID标识符（格式：did:chainpass:xxx）

**✅ 验证点：**
- DID文档是否正确生成
- 公钥是否显示
- 状态是否为「已激活」

### 第四步：完成KYC认证

1. 点击侧边栏「合规中心」→「KYC认证」
2. 填写认证信息：
   - 真实姓名: `测试用户`
   - 国籍: `中国`
   - 证件类型: `身份证`
   - 证件号码: `210202199001011234`
3. 点击「提交认证」

**✅ 验证点：**
- KYC状态变为「审核中」或「已通过」
- 通过后自动签发KYC凭证（VC）

### 第五步：查看可验证凭证

1. 点击侧边栏「身份管理」→「可验证凭证」
2. 查看已签发的凭证列表

**✅ 验证点：**
- 是否有KYCCredential凭证
- 凭证状态是否为「有效」

### 第六步：查看钱包

1. 点击侧边栏「支付中心」→「我的钱包」
2. 查看钱包地址和余额

**✅ 验证点：**
- 钱包地址是否正确生成
- 多币种余额显示（CNY/USD/ETH）

### 第七步：模拟跨境支付

1. 点击侧边栏「支付中心」→「跨境支付」
2. 填写支付信息：
   - 收款人DID: 使用自己的DID（模拟收款）
   - 支付金额: `100`
   - 货币: `CNY`
   - 目标货币: `USD`（可选，测试汇率转换）
3. 点击「创建支付订单」
4. 确认订单信息后点击「确认支付」

**✅ 验证点：**
- 订单创建成功
- 汇率转换正确
- 支付完成提示

### 第八步：查看交易记录

1. 点击侧边栏「支付中心」→「交易记录」
2. 查看刚才的交易记录

---

## 🔐 安全功能测试

### ZKP零知识证明认证

1. 访问 http://localhost:5173/auth/zkp-verify
2. 点击「初始化认证」
3. 等待密钥对生成
4. 点击「签名」
5. 点击「完成认证」

### OAuth第三方登录

需要先配置Gitee OAuth：
1. 在Gitee创建OAuth应用
2. 配置环境变量：
   ```bash
   GITEE_CLIENT_ID=your_client_id
   GITEE_CLIENT_SECRET=your_client_secret
   ```
3. 访问登录页面，点击Gitee图标登录

---

## 📊 系统管理功能测试

### 用户管理

1. 使用管理员账号登录
2. 访问「系统管理」→「用户管理」
3. 测试功能：
   - 查看用户列表
   - 搜索用户
   - 编辑用户信息
   - 分配角色

### 角色权限管理

1. 访问「系统管理」→「角色管理」
2. 测试功能：
   - 创建新角色
   - 分配权限
   - 编辑角色

---

## 🧪 API测试

### 使用Swagger测试

访问 http://localhost:8080/swagger-ui.html

### 使用Postman测试

导入API文档后测试各接口。

---

## 🐛 常见问题

### 1. 数据库连接失败

检查MySQL是否启动，用户名密码是否正确：
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chainpass
    username: root
    password: your_password
```

### 2. Redis连接失败

检查Redis是否启动：
```bash
redis-cli ping
```

### 3. 前端无法连接后端

检查vite配置中的代理设置：
```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 4. DID创建失败

确保：
- 用户已登录
- 数据库连接正常
- 后端服务正常

---

## 📝 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin@123 | 管理员 |
| testuser | Test@123456 | 普通用户 |

---

## 🎯 完整测试检查清单

- [ ] 用户注册成功
- [ ] 用户登录成功
- [ ] DID创建成功
- [ ] DID状态显示正确
- [ ] KYC认证提交成功
- [ ] KYC凭证自动签发
- [ ] 钱包初始化成功
- [ ] 余额显示正确
- [ ] 跨境支付订单创建
- [ ] 汇率转换正确
- [ ] 支付执行成功
- [ ] 交易记录显示正确
- [ ] ZKP认证流程正常
- [ ] 个人资料修改
- [ ] 安全设置配置
- [ ] 用户管理功能
- [ ] 角色管理功能
- [ ] 权限验证正确

---

## 🚢 生产部署

### Docker构建

```bash
# 构建镜像
docker-compose -f docker/docker-compose.yml build

# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d
```

### 环境变量配置

创建 `.env` 文件：
```env
# 数据库
DB_URL=jdbc:mysql://mysql:3306/chainpass
DB_USERNAME=chainpass
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_key_at_least_256_bits

# OAuth
GITEE_CLIENT_ID=your_gitee_client_id
GITEE_CLIENT_SECRET=your_gitee_client_secret
```

---

## 📞 技术支持

如遇问题，请检查：
1. 控制台日志
2. 浏览器开发者工具 Network 面板
3. 后端日志文件
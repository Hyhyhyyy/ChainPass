# =====================================================
# ChainPass 安全配置完整指南
# =====================================================

## 📋 配置清单

| 配置项 | 状态 | 说明 |
|--------|------|------|
| JWT密钥 | ✅ | 使用强密钥，双Token机制 |
| 数据库密码 | ✅ | 生产环境必须修改 |
| Redis密码 | ✅ | 生产环境必须设置 |
| HTTPS/SSL | ✅ | 配置脚本已提供 |
| OAuth配置 | ✅ | 配置指南已提供 |

---

## 🔐 1. JWT密钥配置

### 开发环境

```bash
# .env 文件
JWT_SECRET=dev-chainpass-secret-key-32chars-minimum
```

### 生产环境

**生成强密钥：**
```bash
# 使用 OpenSSL 生成64字符Base64密钥
openssl rand -base64 64 | tr -d '\n' | head -c 64
```

**配置：**
```bash
# .env.production 文件
JWT_SECRET=<生成的强密钥>
JWT_ACCESS_TOKEN_TTL=900000      # 15分钟
JWT_REFRESH_TOKEN_TTL=604800000  # 7天
```

**自动生成脚本：**
```bash
cd chainpass/scripts
./generate-secrets.sh
```

---

## 🗄️ 2. 数据库密码配置

### 开发环境

```bash
# .env 文件
DB_URL=jdbc:mysql://localhost:3306/chainpass?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=chainpass123
```

### 生产环境

**生成强密码：**
```bash
# 生成24字符强密码
openssl rand -base64 24 | tr -d '\n' | head -c 24
```

**配置：**
```bash
# .env.production 文件
DB_URL=jdbc:mysql://localhost:3306/chainpass?useUnicode=true&characterEncoding=utf-8&useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
DB_USERNAME=chainpass              # 使用专用账户，不用root
DB_PASSWORD=<生成的强密码>
```

**创建数据库用户：**
```sql
-- 创建专用用户
CREATE USER 'chainpass'@'%' IDENTIFIED BY '你的强密码';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER ON chainpass.* TO 'chainpass'@'%';
FLUSH PRIVILEGES;
```

---

## 🔒 3. HTTPS/SSL配置

### 开发环境（自签名证书）

**生成证书：**
```bash
cd chainpass/scripts
chmod +x generate-ssl.sh
./generate-ssl.sh
```

**证书位置：**
- `apps/server/src/main/resources/ssl/keystore.p12`

**启用SSL：**
```bash
# .env 文件
SERVER_SSL_ENABLED=true
SSL_KEY_STORE_PASSWORD=chainpass_ssl_password
```

### 生产环境（正规CA证书）

**获取证书：**
1. 购买证书或使用 Let's Encrypt（免费）
2. 证书格式转换为 PKCS12：

```bash
# 转换证书
openssl pkcs12 -export -in cert.pem -inkey key.pem \
  -out keystore.p12 -name chainpass \
  -password pass:你的密码
```

**配置：**
```bash
# .env.production 文件
SERVER_SSL_ENABLED=true
SSL_KEY_STORE=/path/to/keystore.p12
SSL_KEY_STORE_PASSWORD=<你的密码>
SERVER_PORT=443
```

**Let's Encrypt 自动获取（推荐）：**
```bash
# 安装 certbot
apt install certbot

# 获取证书
certbot certonly --standalone -d your-domain.com

# 转换为 PKCS12
openssl pkcs12 -export -in /etc/letsencrypt/live/your-domain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/your-domain.com/privkey.pem \
  -out keystore.p12 -name chainpass
```

---

## 🚀 4. OAuth配置

### 创建 Gitee OAuth 应用

**步骤：**
1. 登录 https://gitee.com
2. 设置 → 第三方应用 → 创建应用
3. 配置：

| 字段 | 值 |
|------|-----|
| 应用名称 | ChainPass |
| 应用主页 | https://your-domain.com |
| 回调地址 | https://your-domain.com/oauth/gitee/callback |
| 权限范围 | user_info |

### 配置环境变量

```bash
# .env.production 文件
GITEE_CLIENT_ID=<Client ID from Gitee>
GITEE_CLIENT_SECRET=<Client Secret from Gitee>
GITEE_REDIRECT_URI=https://your-domain.com/oauth/gitee/callback
OAUTH_STATE_SECRET=<生成的随机字符串>
```

**生成 State Secret：**
```bash
openssl rand -hex 32
```

### 详细配置指南

参考文档：`docs/OAUTH_GUIDE.md`

---

## 📁 5. 环境变量文件

### 开发环境 (.env)

```bash
# 复制模板
cp .env.example .env

# 编辑配置
nano .env
```

### 生产环境 (.env.production)

```bash
# 使用脚本生成
./scripts/generate-secrets.sh

# 或手动创建
cp .env.production.example .env.production
nano .env.production
```

---

## 🔧 6. Redis密码配置

### 开发环境

```bash
# .env 文件
REDIS_PASSWORD=
```

### 生产环境

**生成密码：**
```bash
openssl rand -base64 24 | tr -d '\n' | head -c 24
```

**配置：**
```bash
# .env.production 文件
REDIS_PASSWORD=<生成的密码>
```

**Redis启动命令：**
```bash
redis-server --requirepass <密码>
```

---

## 📊 安全配置检查清单

### 启动前检查

- [ ] JWT_SECRET 已设置为强密钥（≥64字符）
- [ ] 数据库密码已修改（非默认值）
- [ ] Redis密码已设置（生产环境）
- [ ] HTTPS已启用（生产环境）
- [ ] OAuth回调地址配置正确
- [ ] CORS配置了正确的域名
- [ ] 日志级别设置为合理值

### 文件安全检查

- [ ] `.env` 和 `.env.production` 未提交到Git
- [ ] SSL证书私钥文件未提交到Git
- [ ] `keystore.p12` 未提交到Git
- [ ] `.gitignore` 已配置敏感文件排除

### .gitignore 配置

```gitignore
# 环境变量文件
.env
.env.local
.env.production
.env.*.local

# SSL证书
*.p12
*.pem
*.key
keystore.*

# 日志文件
logs/
*.log
```

---

## 🛠️ 快速配置命令

### 一键生成所有密钥

```bash
cd chainpass/scripts
chmod +x generate-secrets.sh generate-ssl.sh

# 生成密钥
./generate-secrets.sh

# 生成SSL证书（可选）
./generate-ssl.sh
```

### 启动服务

```bash
# 开发环境
docker-compose -f docker/docker-compose.yml up -d
cd apps/server && mvn spring-boot:run
cd apps/web && pnpm dev

# 生产环境
# 1. 配置 .env.production
# 2. 启动基础设施
docker-compose -f docker/docker-compose.yml up -d
# 3. 启动应用
java -jar apps/server/target/chainpass-server.jar --spring.profiles.active=prod
```

---

## 📞 常见问题

### Q: Token过期时间如何选择？

推荐配置：
- AccessToken: 15分钟（平衡安全与用户体验）
- RefreshToken: 7天（合理的登录周期）

### Q: 生产环境必须启用HTTPS吗？

是的，理由：
- OAuth回调地址要求HTTPS
- 保护Token传输安全
- 防止中间人攻击
- 满足合规要求

### Q: 密钥多久需要更换？

建议周期：
- JWT_SECRET: 每3-6个月
- 数据库密码: 每6-12个月
- Redis密码: 每6-12个月
- SSL证书: 根据有效期（Let's Encrypt 90天）

---

## 📝 更新记录

| 日期 | 更新内容 |
|------|----------|
| 2024 | 创建安全配置指南 |
| - | 添加JWT密钥配置 |
| - | 添加数据库密码配置 |
| - | 添加SSL证书配置 |
| - | 添加OAuth配置指南 |

---

## 📄 相关文件

| 文件 | 说明 |
|------|------|
| `.env.example` | 开发环境配置模板 |
| `.env.production.example` | 生产环境配置模板 |
| `scripts/generate-secrets.sh` | 密钥生成脚本 |
| `scripts/generate-ssl.sh` | SSL证书生成脚本 |
| `docs/OAUTH_GUIDE.md` | OAuth详细配置指南 |
| `docker/docker-compose.yml` | Docker配置（已更新密码） |
| `apps/server/.../application-prod.yml` | 生产环境配置（已添加SSL） |
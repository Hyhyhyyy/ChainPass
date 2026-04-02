# ChainPass API 文档

## 访问方式

启动后端服务后，访问以下地址查看API文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 认证方式

### Bearer Token 认证

1. 调用 `/auth/login` 获取 access_token
2. 在请求头添加：`Authorization: Bearer {access_token}`

### 示例

```bash
# 登录获取Token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 使用Token访问API
curl http://localhost:8080/did/my \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

## API 分类

### 1. 认证模块 (/auth)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/auth/login` | POST | ❌ | 用户登录 |
| `/auth/register` | POST | ❌ | 用户注册 |
| `/auth/logout` | POST | ✅ | 用户登出 |
| `/auth/refresh` | POST | ❌ | 刷新Token |

### 2. DID模块 (/did)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/did/create` | POST | ✅ | 创建DID |
| `/did/my` | GET | ✅ | 获取我的DID |
| `/did/{did}` | GET | ❌ | 获取DID文档 |
| `/did/verify` | POST | ❌ | 验证DID签名 |
| `/did/revoke` | POST | ✅ | 吊销DID |
| `/did/check/{did}` | GET | ❌ | 检查DID有效性 |

### 3. VC模块 (/vc)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/vc/issue` | POST | ✅ | 签发凭证 |
| `/vc/verify` | POST | ❌ | 验证凭证 |
| `/vc/my` | GET | ✅ | 获取我的凭证 |
| `/vc/list/{did}` | GET | ❌ | 获取DID的凭证 |
| `/vc/revoke/{vcId}` | POST | ✅ | 吊销凭证 |
| `/vc/types` | GET | ❌ | 获取凭证类型 |

### 4. 支付模块 (/payment)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/payment/wallet` | GET | ✅ | 获取钱包 |
| `/payment/create` | POST | ✅ | 创建支付订单 |
| `/payment/execute/{orderNo}` | POST | ✅ | 执行支付 |
| `/payment/history` | GET | ✅ | 交易历史 |
| `/payment/rate/{from}/{to}` | GET | ❌ | 获取汇率 |

### 5. KYC模块 (/kyc)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/kyc/submit` | POST | ✅ | 提交KYC认证 |
| `/kyc/status` | GET | ✅ | 获取KYC状态 |
| `/kyc/detail` | GET | ✅ | 获取KYC详情 |

### 6. 二维码协同模块 (/qr)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/qr/create` | POST | ❌ | 创建二维码会话 |
| `/qr/status/{sessionId}` | GET | ❌ | 查询二维码状态 |
| `/qr/scan/{sessionId}` | POST | ✅ | 扫描二维码 |
| `/qr/confirm/{sessionId}` | POST | ✅ | 确认操作 |
| `/qr/cancel/{sessionId}` | POST | ❌ | 取消操作 |

### 7. ZKP认证模块 (/zkp)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/zkp/init` | POST | ❌ | 初始化ZKP认证 |
| `/zkp/public-key` | POST | ❌ | 提交公钥 |
| `/zkp/verify` | POST | ❌ | 验证认证 |

### 8. OAuth模块 (/oauth)

| 接口 | 方法 | 认证 | 描述 |
|------|------|------|------|
| `/oauth/gitee/config` | GET | ❌ | 获取Gitee OAuth配置 |
| `/oauth/gitee/callback` | GET | ❌ | Gitee OAuth回调 |

---

## 详细接口说明

### 认证接口

#### POST /auth/login

用户登录，获取访问令牌。

**请求体:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "userId": 1,
    "username": "admin",
    "nickname": "系统管理员"
  }
}
```

---

### DID接口

#### POST /did/create

为当前用户创建DID（去中心化身份）。

**请求头:**
```
Authorization: Bearer {token}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "context": ["https://www.w3.org/ns/did/v1"],
    "id": "did:chainpass:a1b2c3d4e5f6...",
    "verificationMethod": [...],
    "authentication": ["did:chainpass:xxx#key-1"],
    "created": "2024-01-01T00:00:00Z"
  }
}
```

#### POST /did/verify

验证DID签名。

**请求体:**
```json
{
  "did": "did:chainpass:xxx",
  "challenge": "random_string",
  "signature": "base64_encoded_signature"
}
```

**响应:**
```json
{
  "code": 200,
  "data": true
}
```

---

### VC接口

#### POST /vc/issue

签发可验证凭证。

**请求体:**
```json
{
  "holderDid": "did:chainpass:xxx",
  "vcType": "KYCCredential",
  "claims": {
    "level": 1,
    "nationality": "China"
  }
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": "urn:uuid:xxx",
    "type": ["VerifiableCredential", "KYCCredential"],
    "issuer": {"id": "did:chainpass:issuer", "name": "ChainPass"},
    "issuanceDate": "2024-01-01T00:00:00Z",
    "expirationDate": "2026-01-01T00:00:00Z",
    "credentialSubject": {...},
    "proof": {...}
  }
}
```

#### POST /vc/verify

验证可验证凭证。

**请求体:**
```json
{
  "vcId": "urn:uuid:xxx"
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "valid": true,
    "vcId": "urn:uuid:xxx",
    "vcType": "KYCCredential",
    "holderDid": "did:chainpass:xxx",
    "message": "凭证验证通过"
  }
}
```

---

### 支付接口

#### GET /payment/wallet

获取用户钱包信息。

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "did": "did:chainpass:xxx",
    "address": "0xabc123...",
    "balanceCny": 10000.00,
    "balanceUsd": 1500.00,
    "balanceEth": 0.5,
    "totalBalanceCny": 21450.00,
    "status": "ACTIVE"
  }
}
```

#### POST /payment/create

创建跨境支付订单。

**请求体:**
```json
{
  "payeeDid": "did:chainpass:yyy",
  "amount": 100.00,
  "currency": "CNY",
  "targetCurrency": "USD",
  "paymentMethod": "wallet",
  "description": "跨境支付测试"
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "orderNo": "PAY17040672000001234",
    "payerDid": "did:chainpass:xxx",
    "payeeDid": "did:chainpass:yyy",
    "amount": 13.89,
    "currency": "USD",
    "originalAmount": 100.00,
    "originalCurrency": "CNY",
    "exchangeRate": 0.1389,
    "feeAmount": 0.01,
    "status": "PENDING"
  }
}
```

#### POST /payment/execute/{orderNo}

执行支付。

**响应:**
```json
{
  "code": 200,
  "data": {
    "orderNo": "PAY17040672000001234",
    "txHash": "tx_abc123...",
    "gateway": "mock",
    "status": 1,
    "confirmedAt": "2024-01-01T00:00:00Z"
  }
}
```

---

### KYC接口

#### POST /kyc/submit

提交KYC认证申请。

**请求体:**
```json
{
  "fullName": "张三",
  "nationality": "China",
  "idType": "id_card",
  "idNumber": "110101199001011234",
  "idDocumentFront": "https://...",
  "idDocumentBack": "https://..."
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "did": "did:chainpass:xxx",
    "kycLevel": 1,
    "kycLevelName": "基础认证",
    "status": "APPROVED",
    "statusName": "已通过",
    "vcId": "urn:uuid:xxx"
  }
}
```

---

## 二维码协同接口

### POST /qr/create

创建二维码会话，用于扫码登录或操作确认。

**请求参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 否 | 会话类型：LOGIN, PAYMENT_CONFIRM, DID_REVOKE，默认LOGIN |

**响应:**
```json
{
  "code": 200,
  "data": {
    "sessionId": "abc123def456",
    "type": "LOGIN",
    "qrContent": "{\"type\":\"LOGIN\",\"sessionId\":\"abc123def456\",\"timestamp\":1704067200000,\"expiresIn\":300,\"callback\":\"/api/qr/confirm\"}",
    "status": "PENDING",
    "createdAt": 1704067200000,
    "expiresAt": 1704067500000
  }
}
```

### GET /qr/status/{sessionId}

查询二维码状态，Web端轮询此接口获取扫码结果。

**响应:**
```json
{
  "code": 200,
  "data": {
    "sessionId": "abc123def456",
    "type": "LOGIN",
    "status": "SCANNED",
    "confirmedBy": 1
  }
}
```

**状态说明:**
- PENDING: 等待扫描
- SCANNED: 已扫描，等待确认
- CONFIRMED: 已确认
- EXPIRED: 已过期

### POST /qr/scan/{sessionId}

移动端扫描二维码后调用此接口。

**请求头:**
```
Authorization: Bearer {token}
```

### POST /qr/confirm/{sessionId}

移动端确认操作。

**请求头:**
```
Authorization: Bearer {token}
```

**请求体:**
```json
{
  "operationType": "LOGIN",
  "extraData": null
}
```

**响应 (登录类型):**
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "userId": 1,
    "username": "admin"
  }
}
```

---

## 深度链接

移动端支持以下深度链接：

| 链接格式 | 说明 |
|---------|------|
| `chainpass://auth/confirm?sessionId=xxx` | 登录确认 |
| `chainpass://did/detail?did=xxx` | DID详情 |
| `chainpass://vc/detail?vcId=xxx` | 凭证详情 |
| `chainpass://payment/confirm?orderNo=xxx` | 支付确认 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**错误响应格式:**
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": null
}
```

---

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| user | user123 | 普通用户 |

---

## Postman 集合

可导出Postman集合进行测试，文件位于：`docs/postman_collection.json`

---

*文档版本: v2.0 | 更新日期: 2024年*
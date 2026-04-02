# =====================================================
# ChainPass Gitee OAuth 配置指南
# =====================================================

## 📋 配置步骤

### 第一步：创建 Gitee OAuth 应用

1. 登录 [Gitee](https://gitee.com)
2. 进入 **设置** → **第三方应用** → **创建应用**
3. 填写应用信息：

| 字段 | 建议值 | 说明 |
|------|--------|------|
| **应用名称** | ChainPass 身份验证系统 | 自定义名称 |
| **应用主页** | https://your-domain.com | 前端应用地址 |
| **应用回调地址** | https://your-domain.com/oauth/gitee/callback | 必须与后端配置一致 |
| **权限范围** | user_info | 获取用户基本信息 |

4. 点击 **创建应用**
5. 记录 **Client ID** 和 **Client Secret**

### 第二步：配置环境变量

在 `.env.production` 文件中添加：

```bash
# Gitee OAuth 配置
GITEE_CLIENT_ID=your_client_id_here
GITEE_CLIENT_SECRET=your_client_secret_here
GITEE_REDIRECT_URI=https://your-domain.com/oauth/gitee/callback
```

### 第三步：配置前端

在前端应用中，需要配置 OAuth 登录按钮：

**apps/web/src/views/auth/LoginView.vue:**

```vue
<template>
  <!-- OAuth 登录按钮 -->
  <div class="oauth-section">
    <el-divider>第三方登录</el-divider>
    <el-button
      class="oauth-btn gitee-btn"
      @click="handleGiteeLogin"
    >
      <img src="@/assets/icons/gitee.svg" alt="Gitee" />
      Gitee 登录
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { oauthApi } from '@/api/oauth'

async function handleGiteeLogin() {
  try {
    const { data } = await oauthApi.getGiteeConfig()
    // 构建OAuth授权URL
    const authUrl = `https://gitee.com/oauth/authorize?client_id=${data.clientId}&redirect_uri=${encodeURIComponent(data.redirectUri)}&response_type=code`
    window.location.href = authUrl
  } catch (error) {
    ElMessage.error('获取OAuth配置失败')
  }
}
</script>
```

### 第四步：测试 OAuth 流程

1. 启动应用：`pnpm dev` / `mvn spring-boot:run`
2. 访问登录页面
3. 点击 "Gitee 登录" 按钮
4. 授权应用访问用户信息
5. 自动回调并完成登录

## 🔐 安全注意事项

### 回调地址验证

- 回调地址必须与 Gitee 应用配置**完全一致**
- 生产环境必须使用 HTTPS
- 回调地址路径固定为：`/oauth/gitee/callback`

### State 参数（防 CSRF）

后端会自动生成并验证 `state` 参数：

```java
// 生成 state
String state = UUID.randomUUID().toString();
redisTemplate.opsForValue().set("oauth:state:" + state, "1", 5, TimeUnit.MINUTES);

// 验证 state
String storedState = redisTemplate.opsForValue().get("oauth:state:" + state);
if (storedState == null) {
    throw new BusinessException("OAuth state 验证失败");
}
```

### Client Secret 保护

- **严禁**将 Client Secret 硬编码在代码中
- **严禁**将 Client Secret 提交到 Git
- 仅存储在后端环境变量中
- 前端只使用 Client ID（可公开）

## 🌍 多环境配置

### 开发环境

```bash
# .env (开发)
GITEE_CLIENT_ID=dev_client_id
GITEE_CLIENT_SECRET=dev_client_secret
GITEE_REDIRECT_URI=http://localhost:8080/oauth/gitee/callback
```

### 生产环境

```bash
# .env.production (生产)
GITEE_CLIENT_ID=prod_client_id
GITEE_CLIENT_SECRET=prod_client_secret
GITEE_REDIRECT_URI=https://chainpass.your-domain.com/oauth/gitee/callback
```

## 📊 OAuth 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                     Gitee OAuth 登录流程                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 用户点击 "Gitee 登录"                                        │
│     ↓                                                            │
│  2. 前端请求 /oauth/gitee/config 获取 client_id                  │
│     ↓                                                            │
│  3. 前端跳转到 Gitee 授权页面                                    │
│     https://gitee.com/oauth/authorize?client_id=xxx              │
│     ↓                                                            │
│  4. 用户在 Gitee 授权                                            │
│     ↓                                                            │
│  5. Gitee 回调到 redirect_uri (携带 code 和 state)              │
│     ↓                                                            │
│  6. 后端验证 state，使用 code 换取 access_token                  │
│     ↓                                                            │
│  7. 后端使用 access_token 获取 Gitee 用户信息                    │
│     ↓                                                            │
│  8. 后端创建/更新本地用户，生成 JWT Token                        │
│     ↓                                                            │
│  9. 重定向到前端，携带 Token                                     │
│     ↓                                                            │
│  10. 前端存储 Token，完成登录                                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 常见问题

### Q: 回调后提示 "redirect_uri_mismatch"

检查：
1. Gitee 应用配置的回调地址
2. 环境变量中的 `GITEE_REDIRECT_URI`
3. 两者必须完全一致（包括协议、端口）

### Q: State 验证失败

原因：
- State 参数过期（5分钟有效期）
- 回调被恶意请求
- Redis 连接问题

解决：
- 检查 Redis 是否正常运行
- 检查网络延迟是否过高

### Q: 如何禁用 OAuth 登录？

在环境变量中清空 Client ID：

```bash
GITEE_CLIENT_ID=
GITEE_CLIENT_SECRET=
```

前端检测到 Client ID 为空时，隐藏 OAuth 登录按钮。

## 📞 技术支持

- Gitee OAuth 文档：https://gitee.com/help/articles/4277
- ChainPass 项目文档：docs/PROJECT_GUIDE.md
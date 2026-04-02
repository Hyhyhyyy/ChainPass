# ChainPass 移动端与Web端协同实现总结

## 架构图

```
┌──────────────────────────────────────────────────────────────────────┐
│                           用户终端                                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐  │
│  │   Vue 3 Web     │    │  Flutter App    │    │     浏览器       │  │
│  │  (PC/H5响应式)   │    │ (iOS/Android)   │    │   (扫码验证)     │  │
│  └────────┬────────┘    └────────┬────────┘    └────────┬────────┘  │
│           │                      │                      │           │
│  ┌────────┴──────────────────────┴──────────────────────┴────────┐  │
│  │                    共享类型模型                                 │  │
│  │  @chainpass/shared/types (TS)  ←→  models.dart (Dart)         │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Spring Boot 后端 API                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ 认证服务  │ │ DID服务  │ │ 支付服务  │ │ KYC服务  │ │ QR服务   │   │
│  │ /auth    │ │ /did     │ │ /payment │ │ /kyc     │ │ /qr      │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                        Redis (会话/缓存)                       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                        MySQL (数据存储)                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

## 已实现的协同功能

### 1. 扫码登录

| 步骤 | Web端 | 移动端 | 后端 |
|------|-------|--------|------|
| 1 | 创建二维码会话 | - | `/qr/create` |
| 2 | 显示二维码 | - | - |
| 3 | 轮询状态 | 扫描二维码 | - |
| 4 | - | 调用扫描接口 | `/qr/scan/{sessionId}` |
| 5 | 显示"已扫描" | 确认登录 | `/qr/confirm/{sessionId}` |
| 6 | 获取Token | - | 返回登录响应 |

**相关文件:**
- 后端: `QRController.java`, `QRLoginService.java`, `QRSession.java`, `QRLoginRequest.java`
- Web: `QRLogin.vue`, `qr.ts`
- 移动端: `qr_scanner_page.dart`, `qr_confirm_page.dart`

### 2. 深度链接

```
chainpass://auth/confirm?sessionId=xxx     → 登录确认页面
chainpass://did/detail?did=xxx             → DID详情页面
chainpass://vc/detail?vcId=xxx             → 凭证详情页面
chainpass://payment/confirm?orderNo=xxx    → 支付确认页面
```

**相关文件:**
- 移动端: `deep_link_service.dart`

### 3. 共享类型模型

| TypeScript (Web) | Dart (Mobile) |
|------------------|---------------|
| `ApiResponse<T>` | `ApiResponse<T>` |
| `LoginResponse` | `LoginResponse` |
| `UserInfo` | `UserInfo` |
| `QRSession` | `QRSession` |
| `DIDDocument` | `DIDDocument` |
| `Wallet` | `Wallet` |
| `KYCStatus` | `KYCStatus` |

**相关文件:**
- Web: `apps/shared/types/index.ts`
- 移动端: `lib/shared/models/models.dart`

## 文件清单

### 新增后端文件

```
apps/server/src/main/java/com/chainpass/
├── controller/
│   └── QRController.java           # 二维码API控制器
├── service/
│   └── QRLoginService.java         # 二维码登录服务
├── dto/
│   └── QRLoginRequest.java         # 二维码登录请求DTO
└── vo/
    └── QRSession.java              # 二维码会话VO
```

### 新增Web端文件

```
apps/web/src/
├── api/
│   └── qr.ts                       # 二维码API
└── components/business/
    └── QRLogin.vue                 # 二维码登录组件
```

### 新增移动端文件

```
apps/mobile/lib/
├── core/
│   ├── services/
│   │   └── deep_link_service.dart  # 深度链接服务
│   └── ... (网络、存储等基础模块)
├── features/
│   ├── auth/                       # 认证模块
│   ├── qr/
│   │   └── presentation/pages/
│   │       ├── qr_scanner_page.dart   # 扫码页面
│   │       └── qr_confirm_page.dart   # 确认页面
│   ├── did/                        # DID模块
│   ├── vc/                         # VC模块
│   ├── payment/                    # 支付模块
│   ├── kyc/                        # KYC模块
│   ├── user/                       # 用户模块
│   └── home/                       # 首页模块
└── shared/
    ├── models/
    │   └── models.dart             # 共享数据模型
    ├── providers/
    │   └── app_provider.dart       # 全局状态
    └── widgets/
        └── common/                 # 通用组件
```

## API 端点

### 二维码协同 API

| 端点 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/qr/create` | POST | ❌ | 创建二维码会话 |
| `/qr/status/{sessionId}` | GET | ❌ | 查询二维码状态 |
| `/qr/scan/{sessionId}` | POST | ✅ | 扫描二维码 |
| `/qr/confirm/{sessionId}` | POST | ✅ | 确认操作 |
| `/qr/cancel/{sessionId}` | POST | ❌ | 取消操作 |

## 使用示例

### Web端扫码登录

```vue
<template>
  <QRLogin
    type="LOGIN"
    title="扫码登录"
    @success="handleLoginSuccess"
    @switch-to-password="showPasswordLogin"
  />
</template>
```

### 移动端扫码

```dart
// 打开扫码页面
Navigator.push(
  context,
  MaterialPageRoute(
    builder: (_) => const QRScannerPage(),
  ),
);

// 或者从深度链接进入
chainpass://auth/confirm?sessionId=xxx
```

## 安全考虑

1. **二维码有效期**: 5分钟过期
2. **会话状态**: 存储在 Redis，支持快速过期
3. **确认用户**: 扫描用户必须与确认用户一致
4. **Token刷新**: 支持自动刷新机制

## 后续优化建议

1. **WebSocket 推送**: 替代轮询，实现实时状态推送
2. **推送通知**: 集成 FCM/APNs，支持离线通知
3. **设备管理**: 实现多设备登录管理
4. **安全增强**: 增加生物识别确认
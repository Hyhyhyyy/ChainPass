# ChainPass 完整流程演示

本演示于 2026-09-02 在本地开发环境中真实运行完成。第 3-20 张业务状态截图来自前端调用 Spring Boot、MySQL 与 Redis 的完整实跑；第 1、2、5 张在移除参赛自检页面后使用生产前端预览重新截取，其中第 5 张仅验证新版仪表盘布局与导航，不把其中的空状态视为新的后端流程证据。

## 演示结果

- 付款方 DID：`did:chainpass:0f7fc00cb1816df581e9fc48eacb2852`
- 收款方 DID：`did:chainpass:cf9f7d51e3440531b15d4fa7da00e26a`
- 支付订单：`PAY17883165467374383`
- 支付金额：`288.00 CNY`
- 沙盒手续费：`0.29 CNY`
- 订单结果：`SUCCESS`
- 付款前 CNY 余额：`10000.00`
- 付款后 CNY 余额：`9711.71`

这里的“支付”是项目明确标注的内部多币种沙盒账本记账，不连接银行、卡组织或真实资金通道；KYC 是授权审核员在本系统内作出的审核结论，不冒充外部持牌机构认证。

## 截图顺序

1. [登录与品牌 Logo](demo-screenshots/01-login-brand.png)
2. [用户注册](demo-screenshots/02-register.png)
3. [注册完成](demo-screenshots/03-registration-complete.png)
4. [首次使用引导](demo-screenshots/04-onboarding.png)
5. [用户仪表盘](demo-screenshots/05-dashboard.png)
6. [创建 DID](demo-screenshots/06-did-create.png)
7. [DID 创建成功](demo-screenshots/07-did-created.png)
8. [KYC 申请完整表单](demo-screenshots/08-kyc-form-full.png)
9. [KYC 表单填写完成](demo-screenshots/09-kyc-filled.png)
10. [KYC 已提交、等待审核](demo-screenshots/10-kyc-pending-full.png)
11. [管理员 KYC 审核队列](demo-screenshots/11-admin-kyc-review.png)
12. [管理员确认批准](demo-screenshots/12-admin-approval-confirm.png)
13. [批准成功并签发凭证](demo-screenshots/13-admin-kyc-approved.png)
14. [沙盒钱包初始化](demo-screenshots/14-wallet-initialized.png)
15. [跨境支付表单填写完成](demo-screenshots/15-transfer-form-filled.png)
16. [合规预检通过并确认订单](demo-screenshots/16-compliance-precheck-passed.png)
17. [支付成功](demo-screenshots/17-payment-success.png)
18. [交易历史与 SUCCESS 状态](demo-screenshots/18-transaction-history.png)
19. [有效的可验证凭证](demo-screenshots/19-verifiable-credential.png)
20. [付款后余额与最近交易](demo-screenshots/20-wallet-after-payment.png)

## 实际调用链

用户注册与登录 → 创建 DID 和 Ed25519 密钥 → 提交身份资料 → 授权审核员批准 → 签发审核结论凭证 → 初始化沙盒钱包 → 校验收付款双方 DID/KYC/凭证 → 合规规则评分 → 创建订单 → 数据库事务原子扣款、收款与手续费记账 → 写入交易历史。

## 本次实跑发现并修复的问题

1. Redis JSON 反序列化后得到通用 JSON 对象和整数类型，无法直接转换成 `LoginUser`、`DIDDocument`、`Long` 与 `BigDecimal`。现已增加显式目标类型转换，并补充回归测试。
2. KYC 审批向不可变的 `Map.of(...)` 写入凭证声明，导致审批事务失败。现已在凭证服务中复制为可变映射，并用不可变输入补充测试。
3. MySQL 初始化脚本未显式设置客户端字符集，中文种子数据可能被双重编码。现已加入 `SET NAMES utf8mb4`，并验证凭证中文名称正常显示。

## 复核方法

- 后端测试：在 JDK 17 环境执行 `mvn -f apps/server/pom.xml test`。
- 前端构建：执行 `pnpm --filter @chainpass/web build`。
- 服务运行：使用 `docker/docker-compose.yml` 启动 MySQL 与 Redis，再启动后端和前端。
- 账本恒等式：`10000.00 - 288.00 - 0.29 = 9711.71`，与第 14、16、18、20 张截图一致。

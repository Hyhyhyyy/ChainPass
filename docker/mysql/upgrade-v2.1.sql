-- ChainPass 数据库升级脚本 V2.1
-- 为现有数据库添加新字段

-- 1. 为钱包表添加乐观锁版本号字段
ALTER TABLE pay_wallet ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER frozen_usd;

-- 2. 为VC记录表添加状态更新方法（如果不存在）
-- 注：以下操作在代码层面完成

-- 3. 为DID表添加私钥存储字段（如果不存在）
-- ALTER TABLE chain_did ADD COLUMN private_key_encrypted TEXT COMMENT '加密后的私钥' AFTER public_key;

-- 4. 添加索引优化
CREATE INDEX IF NOT EXISTS idx_pay_order_did ON pay_order (payer_did, payee_did);

-- 5. 为登录日志表添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_login_log_user ON sys_login_log (user_id, login_at);

-- 验证升级
SELECT 'Wallet version column added' AS status FROM pay_wallet LIMIT 1;
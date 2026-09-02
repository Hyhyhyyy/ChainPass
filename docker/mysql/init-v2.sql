-- =====================================================
-- ChainPass v2.0 数据库初始化脚本
-- 本地数字身份、签名凭证与沙盒多币种账本
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS chainpass DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chainpass;

-- =====================================================
-- 1. 用户与权限系统（已有，保留）
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    nickname VARCHAR(50) COMMENT '昵称',
    password VARCHAR(255) COMMENT '密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-停用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    gitee_id VARCHAR(50) COMMENT '保留兼容字段，当前不使用',
    scope VARCHAR(255) COMMENT '保留兼容字段，当前不使用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    type TINYINT COMMENT '类型：1-菜单 2-按钮 3-API',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(255) COMMENT '路由路径',
    component VARCHAR(255) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_type VARCHAR(20) COMMENT '登录类型：password',
    ip VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '地理位置',
    device VARCHAR(255) COMMENT '设备信息',
    browser VARCHAR(100) COMMENT '浏览器',
    os VARCHAR(100) COMMENT '操作系统',
    status TINYINT COMMENT '状态：0-成功 1-失败',
    message VARCHAR(255) COMMENT '消息',
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    INDEX idx_user_id (user_id),
    INDEX idx_login_at (login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_desc VARCHAR(255) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '地理位置',
    execution_time INT COMMENT '执行时间(ms)',
    status TINYINT COMMENT '状态：0-成功 1-失败',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================
-- 2. DID去中心化身份系统（新增）
-- =====================================================

-- DID身份表
CREATE TABLE IF NOT EXISTS chain_did (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    did VARCHAR(255) NOT NULL UNIQUE COMMENT 'DID标识符: did:chainpass:xxx',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    public_key TEXT NOT NULL COMMENT 'Ed25519公钥(Base64编码)',
    private_key_encrypted TEXT COMMENT '加密后的私钥(用户自行管理)',
    did_document TEXT NOT NULL COMMENT '完整DID文档JSON',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-激活 1-停用 2-吊销',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    revoked_at TIMESTAMP NULL COMMENT '吊销时间',
    revoke_reason VARCHAR(255) COMMENT '吊销原因',
    INDEX idx_did (did),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='去中心化身份表';

-- DID操作日志表
CREATE TABLE IF NOT EXISTS chain_did_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    did VARCHAR(255) NOT NULL COMMENT 'DID标识符',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/UPDATE/REVOKE/VERIFY',
    operator_id BIGINT COMMENT '操作人ID',
    detail TEXT COMMENT '操作详情JSON',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_did (did),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DID操作日志表';

-- =====================================================
-- 3. VC可验证凭证系统（新增）
-- =====================================================

-- VC类型定义表
CREATE TABLE IF NOT EXISTS chain_vc_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
    type_name_en VARCHAR(100) COMMENT '英文名称',
    schema_json TEXT COMMENT 'JSON Schema定义',
    validity_days INT DEFAULT 365 COMMENT '默认有效期(天)',
    description VARCHAR(500) COMMENT '描述',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-启用 1-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VC类型定义表';

-- 可验证凭证表
CREATE TABLE IF NOT EXISTS chain_vc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    vc_id VARCHAR(255) NOT NULL UNIQUE COMMENT 'VC唯一标识: urn:uuid:xxx',
    holder_did VARCHAR(255) NOT NULL COMMENT '持有者DID',
    issuer_did VARCHAR(255) NOT NULL DEFAULT 'did:chainpass:issuer' COMMENT '签发者DID',
    vc_type VARCHAR(50) NOT NULL COMMENT '凭证类型编码',
    vc_data TEXT NOT NULL COMMENT '完整VC JSON数据',
    credential_hash VARCHAR(64) NOT NULL COMMENT '凭证内容哈希(SHA256)',
    signature TEXT NOT NULL COMMENT 'Ed25519签名(Base64)',
    ipfs_hash VARCHAR(255) COMMENT 'IPFS存储哈希(预留)',
    blockchain_tx_hash VARCHAR(255) COMMENT '保留字段，当前不使用',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-有效 1-过期 2-吊销',
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '签发时间',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    revoked_at TIMESTAMP NULL COMMENT '吊销时间',
    revoke_reason VARCHAR(255) COMMENT '吊销原因',
    INDEX idx_holder_did (holder_did),
    INDEX idx_vc_id (vc_id),
    INDEX idx_vc_type (vc_type),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可验证凭证表';

-- VC验证记录表
CREATE TABLE IF NOT EXISTS chain_vc_verify_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    vc_id VARCHAR(255) NOT NULL COMMENT 'VC标识',
    verifier_did VARCHAR(255) COMMENT '验证者DID',
    verify_result TINYINT NOT NULL COMMENT '验证结果: 0-成功 1-失败',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    verify_ip VARCHAR(50) COMMENT '验证IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    INDEX idx_vc_id (vc_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VC验证记录表';

-- =====================================================
-- 4. 支付系统（新增）
-- =====================================================

-- 钱包表
CREATE TABLE IF NOT EXISTS pay_wallet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    did VARCHAR(255) NOT NULL COMMENT '用户DID',
    wallet_address VARCHAR(255) NOT NULL COMMENT '钱包地址',
    balance_cny DECIMAL(18,2) DEFAULT 0.00 COMMENT '人民币余额',
    balance_usd DECIMAL(18,2) DEFAULT 0.00 COMMENT '美元余额',
    balance_eth DECIMAL(18,8) DEFAULT 0.00000000 COMMENT 'ETH余额',
    frozen_cny DECIMAL(18,2) DEFAULT 0.00 COMMENT '冻结人民币',
    frozen_usd DECIMAL(18,2) DEFAULT 0.00 COMMENT '冻结美元',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-正常 1-冻结 2-注销',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_did (did),
    INDEX idx_wallet_address (wallet_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付钱包表';

-- 支付订单表
CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    payer_did VARCHAR(255) NOT NULL COMMENT '付款人DID',
    payee_did VARCHAR(255) NOT NULL COMMENT '收款人DID',
    payer_wallet_id BIGINT COMMENT '付款人钱包ID',
    payee_wallet_id BIGINT COMMENT '收款人钱包ID',
    amount DECIMAL(28,8) NOT NULL COMMENT '到账金额',
    currency VARCHAR(10) NOT NULL COMMENT '支付货币: CNY/USD/ETH',
    original_amount DECIMAL(28,8) COMMENT '汇出金额',
    original_currency VARCHAR(10) COMMENT '原始货币',
    exchange_rate DECIMAL(10,6) COMMENT '汇率',
    fee_amount DECIMAL(28,8) DEFAULT 0.00000000 COMMENT '手续费',
    fee_currency VARCHAR(10) COMMENT '手续费币种',
    payment_method VARCHAR(20) DEFAULT 'wallet' COMMENT '记账方式: wallet',
    payment_purpose VARCHAR(100) COMMENT '支付目的',
    description VARCHAR(500) COMMENT '支付描述',
    source_country VARCHAR(2) NOT NULL COMMENT '汇出国家或地区代码',
    target_country VARCHAR(2) NOT NULL COMMENT '收款国家或地区代码',
    beneficiary_name VARCHAR(100) NOT NULL COMMENT '受益人姓名',
    vc_required VARCHAR(50) COMMENT '需要的VC类型',
    vc_verified TINYINT DEFAULT 0 COMMENT 'VC验证状态: 0-未验证 1-已验证',
    kyc_required TINYINT DEFAULT 0 COMMENT '是否需要KYC',
    kyc_verified TINYINT DEFAULT 0 COMMENT 'KYC验证状态',
    risk_score INT DEFAULT 0 COMMENT '风控评分(0-100)',
    risk_level VARCHAR(20) DEFAULT 'LOW' COMMENT '风险等级: LOW/MEDIUM/HIGH',
    compliance_decision VARCHAR(30) COMMENT '规则或人工合规决策',
    compliance_reasons VARCHAR(1000) COMMENT '可解释的命中规则与复核意见',
    reviewed_by BIGINT COMMENT '支付合规复核人',
    reviewed_at TIMESTAMP NULL COMMENT '支付合规复核时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-处理中 2-成功 3-失败 4-已退款 5-合规复核 6-合规拒绝',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    expired_at TIMESTAMP COMMENT '订单过期时间',
    INDEX idx_order_no (order_no),
    INDEX idx_payer_did (payer_did),
    INDEX idx_payee_did (payee_did),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 支付交易记录表
CREATE TABLE IF NOT EXISTS pay_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    tx_hash VARCHAR(255) COMMENT '内部交易标识',
    gateway VARCHAR(20) NOT NULL COMMENT '处理通道: internal-ledger',
    gateway_tx_id VARCHAR(255) COMMENT '网关交易ID',
    from_address VARCHAR(255) COMMENT '付款地址',
    to_address VARCHAR(255) COMMENT '收款地址',
    amount DECIMAL(28,8) NOT NULL COMMENT '交易金额',
    currency VARCHAR(10) NOT NULL COMMENT '货币',
    fee_amount DECIMAL(28,8) DEFAULT 0 COMMENT '手续费',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-处理中 1-成功 2-失败',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    confirmed_at TIMESTAMP NULL COMMENT '确认时间',
    INDEX idx_order_no (order_no),
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_gateway_tx_id (gateway_tx_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付交易记录表';

-- 汇率表
CREATE TABLE IF NOT EXISTS pay_exchange_rate (
    from_currency VARCHAR(10) NOT NULL COMMENT '源货币',
    to_currency VARCHAR(10) NOT NULL COMMENT '目标货币',
    rate DECIMAL(20,8) NOT NULL COMMENT '汇率',
    source VARCHAR(20) DEFAULT 'manual' COMMENT '数据来源: manual/api',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (from_currency, to_currency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率表';

-- =====================================================
-- 5. 合规系统（新增）
-- =====================================================

-- KYC认证表
CREATE TABLE IF NOT EXISTS comp_kyc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    did VARCHAR(255) NOT NULL COMMENT '用户DID',
    kyc_level TINYINT DEFAULT 0 COMMENT 'KYC等级: 0-未认证 1-基础 2-中级 3-高级',
    full_name VARCHAR(100) COMMENT '真实姓名',
    name_encrypted VARCHAR(255) COMMENT '加密姓名',
    nationality VARCHAR(50) COMMENT '国籍',
    country_code VARCHAR(10) COMMENT '国家代码',
    id_type VARCHAR(20) COMMENT '证件类型: id_card/passport/driver_license',
    id_number VARCHAR(100) COMMENT '证件号码',
    id_number_encrypted VARCHAR(255) COMMENT '加密证件号',
    id_document_front VARCHAR(255) COMMENT '证件正面照片URL/IPFS',
    id_document_back VARCHAR(255) COMMENT '证件背面照片URL/IPFS',
    face_photo VARCHAR(255) COMMENT '人脸照片URL/IPFS',
    address VARCHAR(500) COMMENT '地址',
    address_document VARCHAR(255) COMMENT '地址证明文件',
    verification_status TINYINT DEFAULT 0 COMMENT '审核状态: 0-待提交 1-审核中 2-通过 3-拒绝',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    verified_by BIGINT COMMENT '审核人ID',
    verified_at TIMESTAMP NULL COMMENT '认证通过时间',
    expires_at TIMESTAMP COMMENT '认证过期时间',
    vc_id VARCHAR(255) COMMENT '签发的KYC凭证ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    INDEX idx_user_id (user_id),
    INDEX idx_did (did),
    INDEX idx_kyc_level (kyc_level),
    INDEX idx_status (verification_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KYC认证表';

-- =====================================================
-- 6. 初始化数据
-- =====================================================

-- 初始化管理员账号
INSERT INTO sys_user (username, nickname, password, email, status, del_flag)
VALUES ('admin', '系统管理员', '$2a$12$tcLOZYRbl58Pcp9zv.1OgO8dPP53ZH1EgF8hupZCFMitVN75lqSky', 'admin@chainpass.com', 0, 0)
ON DUPLICATE KEY UPDATE username = username;
-- 仅本地开发初始密码: AdminPass2026!；首次登录后必须修改。

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'admin', '拥有所有权限'),
('普通用户', 'user', '普通用户权限')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 初始化权限
INSERT INTO sys_permission (permission_name, permission_code, type, parent_id, path, component, icon, sort_order) VALUES
('系统管理', 'system', 1, 0, '/system', NULL, 'Setting', 1),
('用户管理', 'system:user:list', 1, 1, '/system/users', 'system/UserManage', 'User', 1),
('新增用户', 'system:user:add', 3, 1, NULL, NULL, NULL, 2),
('编辑用户', 'system:user:edit', 3, 1, NULL, NULL, NULL, 3),
('删除用户', 'system:user:delete', 3, 1, NULL, NULL, NULL, 4),
('角色管理', 'system:role:list', 1, 1, '/system/roles', 'system/RoleManage', 'Avatar', 2),
('新增角色', 'system:role:add', 3, 1, NULL, NULL, NULL, 5),
('编辑角色', 'system:role:edit', 3, 1, NULL, NULL, NULL, 6),
('删除角色', 'system:role:delete', 3, 1, NULL, NULL, NULL, 7),
('身份管理', 'identity', 1, 0, '/identity', NULL, 'Key', 2),
('DID管理', 'identity:did:list', 1, 2, '/identity/did', 'identity/DIDManage', 'Postcard', 1),
('凭证管理', 'identity:vc:list', 1, 2, '/identity/vc', 'identity/VCList', 'Tickets', 2),
('支付中心', 'payment', 1, 0, '/payment', NULL, 'Wallet', 3),
('我的钱包', 'payment:wallet', 1, 3, '/payment/wallet', 'payment/Wallet', 'Wallet', 1),
('跨境合规支付', 'payment:transfer', 1, 3, '/payment/transfer', 'payment/Transfer', 'Coin', 2),
('合规中心', 'compliance', 1, 0, '/compliance', NULL, 'Shield', 4),
('KYC认证', 'compliance:kyc', 1, 4, '/compliance/kyc', 'compliance/KYCApply', 'Checked', 1),
('KYC审核', 'compliance:kyc:audit', 3, 4, NULL, NULL, NULL, 2),
('支付合规复核', 'compliance:payment:audit', 3, 4, NULL, NULL, NULL, 3)
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 关联角色权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 初始化VC类型
INSERT INTO chain_vc_type (type_code, type_name, type_name_en, validity_days, description, icon, sort_order) VALUES
('KYCCredential', '人工审核结论凭证', 'Review Decision Credential', 730, '证明 ChainPass 授权审核员已通过该申请，不代表外部机构KYC', 'Stamp', 1)
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);

-- 初始化固定沙盒汇率
INSERT INTO pay_exchange_rate (from_currency, to_currency, rate, source) VALUES
('CNY', 'USD', 0.1389, 'manual'),
('USD', 'CNY', 7.2000, 'manual'),
('CNY', 'ETH', 0.000052, 'manual'),
('ETH', 'CNY', 19230.7700, 'manual'),
('USD', 'ETH', 0.00037, 'manual'),
('ETH', 'USD', 2702.7000, 'manual')
ON DUPLICATE KEY UPDATE rate = VALUES(rate);

-- =====================================================
-- 完成
-- =====================================================
SELECT 'ChainPass v2.0 Database initialized successfully!' AS message;

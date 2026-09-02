-- ChainPass V2.2 跨境合规支付原型升级（仅执行一次，先备份数据库）
ALTER TABLE pay_order
    ADD COLUMN source_country VARCHAR(2) NULL COMMENT '汇出国家或地区代码' AFTER description,
    ADD COLUMN target_country VARCHAR(2) NULL COMMENT '收款国家或地区代码' AFTER source_country,
    ADD COLUMN beneficiary_name VARCHAR(100) NULL COMMENT '受益人姓名' AFTER target_country,
    ADD COLUMN compliance_decision VARCHAR(30) NULL COMMENT '规则或人工合规决策' AFTER risk_level,
    ADD COLUMN compliance_reasons VARCHAR(1000) NULL COMMENT '命中规则与复核意见' AFTER compliance_decision,
    ADD COLUMN reviewed_by BIGINT NULL COMMENT '支付合规复核人' AFTER compliance_reasons,
    ADD COLUMN reviewed_at TIMESTAMP NULL COMMENT '支付合规复核时间' AFTER reviewed_by,
    MODIFY COLUMN amount DECIMAL(28,8) NOT NULL COMMENT '到账金额',
    MODIFY COLUMN original_amount DECIMAL(28,8) NULL COMMENT '汇出金额',
    MODIFY COLUMN fee_amount DECIMAL(28,8) DEFAULT 0.00000000 COMMENT '手续费';

ALTER TABLE pay_transaction
    MODIFY COLUMN amount DECIMAL(28,8) NOT NULL COMMENT '交易金额',
    MODIFY COLUMN fee_amount DECIMAL(28,8) DEFAULT 0 COMMENT '手续费';

INSERT INTO sys_permission
    (permission_name, permission_code, type, parent_id, path, component, icon, sort_order)
VALUES
    ('支付合规复核', 'compliance:payment:audit', 3,
     (SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'compliance' LIMIT 1) AS compliance_parent),
     NULL, NULL, NULL, 3)
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE permission_code = 'compliance:payment:audit'
ON DUPLICATE KEY UPDATE role_id = role_id;

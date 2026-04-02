package com.chainpass.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二维码会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRSession {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 二维码类型: LOGIN, PAYMENT_CONFIRM, DID_REVOKE, etc.
     */
    private String type;

    /**
     * 二维码内容 (JSON格式，用于客户端解析)
     */
    private String qrContent;

    /**
     * 状态: PENDING, SCANNED, CONFIRMED, EXPIRED
     */
    private String status;

    /**
     * 创建时间戳
     */
    private Long createdAt;

    /**
     * 过期时间戳
     */
    private Long expiresAt;

    /**
     * 确认用户ID (已确认时)
     */
    private Long confirmedBy;

    /**
     * 关联数据
     */
    private Object data;
}
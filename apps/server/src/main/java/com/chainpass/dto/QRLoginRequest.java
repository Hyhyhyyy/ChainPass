package com.chainpass.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 二维码登录请求
 */
@Data
public class QRLoginRequest {
    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    /**
     * 操作类型: LOGIN, PAYMENT_CONFIRM, DID_REVOKE, etc.
     */
    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    /**
     * 额外数据 (JSON格式)
     */
    private String extraData;
}
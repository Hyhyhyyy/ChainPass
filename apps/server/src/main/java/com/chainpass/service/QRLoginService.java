package com.chainpass.service;

import com.chainpass.dto.QRLoginRequest;
import com.chainpass.vo.QRSession;
import com.chainpass.vo.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 二维码登录服务
 * 实现移动端与Web端的扫码登录协同
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QRLoginService {

    private final StringRedisTemplate redisTemplate;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    private static final String QR_SESSION_PREFIX = "qr:session:";
    private static final long QR_EXPIRE_SECONDS = 300; // 5分钟过期

    /**
     * 创建二维码会话
     * @param type 会话类型 (LOGIN, PAYMENT_CONFIRM, etc.)
     * @return 二维码会话信息
     */
    public QRSession createSession(String type) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();
        long expiresAt = now + QR_EXPIRE_SECONDS * 1000;

        // 构建二维码内容
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("type", type);
        qrData.put("sessionId", sessionId);
        qrData.put("timestamp", now);
        qrData.put("expiresIn", QR_EXPIRE_SECONDS);
        qrData.put("callback", "/api/qr/confirm");

        String qrContent;
        try {
            qrContent = objectMapper.writeValueAsString(qrData);
        } catch (Exception e) {
            log.error("构建二维码内容失败", e);
            qrContent = "{}";
        }

        QRSession session = QRSession.builder()
                .sessionId(sessionId)
                .type(type)
                .qrContent(qrContent)
                .status("PENDING")
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        // 存储到 Redis
        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(
                    QR_SESSION_PREFIX + sessionId,
                    sessionJson,
                    QR_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("存储二维码会话失败", e);
        }

        return session;
    }

    /**
     * 获取会话状态
     * Web端轮询此接口获取扫码结果
     */
    public QRSession getSession(String sessionId) {
        String json = redisTemplate.opsForValue().get(QR_SESSION_PREFIX + sessionId);
        if (json == null) {
            return QRSession.builder()
                    .sessionId(sessionId)
                    .status("EXPIRED")
                    .build();
        }

        try {
            return objectMapper.readValue(json, QRSession.class);
        } catch (Exception e) {
            log.error("解析会话数据失败", e);
            return null;
        }
    }

    /**
     * 扫描二维码
     * 移动端调用此接口标记已扫描
     */
    public boolean scan(String sessionId, Long userId) {
        QRSession session = getSession(sessionId);
        if (session == null || "EXPIRED".equals(session.getStatus())) {
            return false;
        }

        session.setStatus("SCANNED");
        session.setConfirmedBy(userId);

        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            // 计算剩余过期时间
            long ttl = (session.getExpiresAt() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        QR_SESSION_PREFIX + sessionId,
                        sessionJson,
                        ttl,
                        TimeUnit.SECONDS
                );
            }
            return true;
        } catch (Exception e) {
            log.error("更新会话状态失败", e);
            return false;
        }
    }

    /**
     * 确认操作
     * 移动端确认登录或其他操作
     */
    public LoginResponse confirm(String sessionId, Long userId, QRLoginRequest request) {
        QRSession session = getSession(sessionId);
        if (session == null || "EXPIRED".equals(session.getStatus())) {
            throw new RuntimeException("二维码已过期");
        }

        if (!userId.equals(session.getConfirmedBy())) {
            throw new RuntimeException("非扫描用户无法确认");
        }

        // 根据操作类型处理
        switch (request.getOperationType()) {
            case "LOGIN":
                // 生成登录响应
                LoginResponse loginResponse = authService.generateLoginResponse(userId);

                // 更新会话状态
                session.setStatus("CONFIRMED");
                session.setData(loginResponse);
                updateSession(session);

                return loginResponse;

            case "PAYMENT_CONFIRM":
                // 支付确认逻辑
                session.setStatus("CONFIRMED");
                session.setData(Map.of("paymentConfirmed", true));
                updateSession(session);
                return null;

            case "DID_REVOKE":
                // DID吊销确认逻辑
                session.setStatus("CONFIRMED");
                session.setData(Map.of("revokeConfirmed", true));
                updateSession(session);
                return null;

            default:
                throw new RuntimeException("未知的操作类型: " + request.getOperationType());
        }
    }

    /**
     * 取消操作
     */
    public void cancel(String sessionId) {
        redisTemplate.delete(QR_SESSION_PREFIX + sessionId);
    }

    /**
     * 更新会话
     */
    private void updateSession(QRSession session) {
        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            long ttl = (session.getExpiresAt() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        QR_SESSION_PREFIX + sessionId,
                        sessionJson,
                        ttl,
                        TimeUnit.SECONDS
                );
            }
        } catch (Exception e) {
            log.error("更新会话失败", e);
        }
    }
}
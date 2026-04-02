package com.chainpass.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具类
 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (isEmpty(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isEmpty(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (isEmpty(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isEmpty(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isEmpty(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IP;
        }

        return ip;
    }

    /**
     * 获取IP归属地（简化版，实际需要调用第三方服务）
     */
    public static String getLocation(HttpServletRequest request) {
        String ip = getIpAddress(request);
        // 本地IP
        if (LOCALHOST_IP.equals(ip) || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "本地局域网";
        }
        // 实际项目中可以调用 IP 归属地查询服务
        return "未知";
    }

    /**
     * 判断IP是否为空
     */
    private static boolean isEmpty(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
}
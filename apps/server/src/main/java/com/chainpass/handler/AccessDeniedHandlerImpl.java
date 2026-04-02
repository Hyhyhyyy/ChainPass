package com.chainpass.handler;

import com.alibaba.fastjson2.JSON;
import com.chainpass.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 访问拒绝处理器
 */

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("Access denied: {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> result = ApiResponse.error(403, "无权访问");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
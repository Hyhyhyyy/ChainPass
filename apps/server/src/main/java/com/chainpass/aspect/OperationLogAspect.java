package com.chainpass.aspect;

import com.alibaba.fastjson2.JSON;
import com.chainpass.entity.LoginUser;
import com.chainpass.entity.OperationLog;
import com.chainpass.mapper.OperationLogMapper;
import com.chainpass.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 操作日志切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, com.chainpass.annotation.OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            try {
                saveLog(point, operationLog, result, startTime, exception);
            } catch (Exception e) {
                log.error("保存操作日志失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint point, com.chainpass.annotation.OperationLog operationLog,
                         Object result, long startTime, Throwable exception) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        String username = "anonymous";

        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            userId = loginUser.getUser().getId();
            username = loginUser.getUser().getUsername();
        }

        // 获取请求信息
        HttpServletRequest request = getRequest();
        if (request == null) return;

        // 构建日志实体
        OperationLog logEntity = new OperationLog();
        logEntity.setUserId(userId);
        logEntity.setUsername(username);
        logEntity.setOperationType(operationLog.type());
        logEntity.setOperationDesc(operationLog.value());
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setRequestUrl(request.getRequestURI());
        logEntity.setIp(IpUtils.getIpAddress(request));
        logEntity.setLocation(IpUtils.getLocation(request));
        logEntity.setExecutionTime((int) (System.currentTimeMillis() - startTime));
        logEntity.setStatus(exception == null ? 0 : 1);
        logEntity.setCreatedAt(LocalDateTime.now());

        // 请求参数
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();

        if (paramNames != null && args != null) {
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                // 过滤掉不需要记录的参数
                if (arg instanceof ServletRequest || arg instanceof ServletResponse
                        || arg instanceof MultipartFile) {
                    continue;
                }
                params.put(paramNames[i], arg);
            }
            if (!params.isEmpty()) {
                logEntity.setRequestParams(JSON.toJSONString(params));
            }
        }

        // 响应结果
        if (result != null) {
            String resultStr = JSON.toJSONString(result);
            if (resultStr.length() > 2000) {
                resultStr = resultStr.substring(0, 2000) + "...";
            }
            logEntity.setResponseResult(resultStr);
        }

        // 异常信息
        if (exception != null) {
            logEntity.setResponseResult(exception.getMessage());
        }

        // 保存日志
        operationLogMapper.insert(logEntity);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
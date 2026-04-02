package com.chainpass.controller;

import com.chainpass.service.ZKPService;
import com.chainpass.vo.ApiResponse;
import com.chainpass.vo.LoginResponse;
import com.chainpass.vo.ZKPChallenge;
import com.chainpass.dto.ZKPVerifyRequest;
import com.chainpass.dto.ZKPInitRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

/**
 * ZKP 零知识证明认证控制器
 */

@RestController
@RequestMapping("/zkp")
@RequiredArgsConstructor
public class ZKPController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZKPController.class);

    private final ZKPService zkpService;

    /**
     * 初始化 ZKP 认证
     * 生成挑战返回给前端
     */
    @PostMapping("/init")
    public ApiResponse<ZKPChallenge> initAuth() {
        log.info("ZKP auth initialization");
        ZKPChallenge challenge = zkpService.initAuth();
        return ApiResponse.success(challenge);
    }

    /**
     * 提交公钥
     * 前端将公钥提交到后端存储
     */
    @PostMapping("/public-key")
    public ApiResponse<Void> submitPublicKey(@Valid @RequestBody ZKPInitRequest request) {
        log.info("ZKP public key submission for session: {}", request.getSessionId());
        zkpService.storePublicKey(request.getSessionId(), request.getPublicKey(), request.getJwt());
        return ApiResponse.success();
    }

    /**
     * 验证 ZKP 认证
     * 验证签名并返回登录凭证
     */
    @PostMapping("/verify")
    public ApiResponse<LoginResponse> verifyAuth(@Valid @RequestBody ZKPVerifyRequest request) {
        log.info("ZKP verification for session: {}", request.getSessionId());
        LoginResponse response = zkpService.verifyAuth(request);
        return ApiResponse.success(response);
    }

    /**
     * 检查认证状态
     */
    @GetMapping("/status/{sessionId}")
    public ApiResponse<Boolean> checkStatus(@PathVariable String sessionId) {
        boolean isValid = zkpService.checkSession(sessionId);
        return ApiResponse.success(isValid);
    }
}
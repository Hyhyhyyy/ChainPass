package com.chainpass.controller;

import com.chainpass.dto.QRLoginRequest;
import com.chainpass.service.QRLoginService;
import com.chainpass.vo.ApiResponse;
import com.chainpass.vo.QRSession;
import com.chainpass.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 二维码登录控制器
 * 实现移动端与Web端的扫码协同
 */
@Tag(name = "二维码登录", description = "扫码登录相关接口")
@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class QRController {

    private final QRLoginService qrLoginService;

    /**
     * 创建登录二维码
     * Web端调用此接口获取二维码内容
     */
    @Operation(summary = "创建登录二维码")
    @PostMapping("/create")
    public ApiResponse<QRSession> createSession(@RequestParam(defaultValue = "LOGIN") String type) {
        QRSession session = qrLoginService.createSession(type);
        return ApiResponse.success(session);
    }

    /**
     * 查询二维码状态
     * Web端轮询此接口获取扫码结果
     */
    @Operation(summary = "查询二维码状态")
    @GetMapping("/status/{sessionId}")
    public ApiResponse<QRSession> getStatus(@PathVariable String sessionId) {
        QRSession session = qrLoginService.getSession(sessionId);
        return ApiResponse.success(session);
    }

    /**
     * 扫描二维码
     * 移动端调用此接口标记已扫描
     */
    @Operation(summary = "扫描二维码")
    @PostMapping("/scan/{sessionId}")
    public ApiResponse<Void> scan(
            @PathVariable String sessionId,
            @AuthenticationPrincipal Long userId
    ) {
        boolean success = qrLoginService.scan(sessionId, userId);
        return success ? ApiResponse.success() : ApiResponse.error("扫描失败");
    }

    /**
     * 确认操作
     * 移动端确认登录或其他操作
     */
    @Operation(summary = "确认操作")
    @PostMapping("/confirm/{sessionId}")
    public ApiResponse<LoginResponse> confirm(
            @PathVariable String sessionId,
            @Valid @RequestBody QRLoginRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        LoginResponse response = qrLoginService.confirm(sessionId, userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 取消操作
     */
    @Operation(summary = "取消操作")
    @PostMapping("/cancel/{sessionId}")
    public ApiResponse<Void> cancel(@PathVariable String sessionId) {
        qrLoginService.cancel(sessionId);
        return ApiResponse.success();
    }
}
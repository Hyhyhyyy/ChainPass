package com.chainpass.compliance.kyc;

import com.chainpass.entity.LoginUser;
import com.chainpass.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * KYC控制器 - 身份认证API
 */
@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC认证", description = "身份认证管理接口")
public class KYCController {

    private static final Logger log = LoggerFactory.getLogger(KYCController.class);

    private final KYCService kycService;

    /**
     * 提交KYC认证
     */
    @PostMapping("/submit")
    @Operation(summary = "提交KYC", description = "提交KYC身份认证申请")
    public ApiResponse<KYCDto.KYCResponse> submitKYC(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody KYCDto.KYCSubmitRequest request) {

        log.info("Submitting KYC for user: {}", loginUser.getUserId());

        KYCRecord record = kycService.submitKYC(loginUser.getUserId(), request);

        return ApiResponse.success(kycService.getKYCDetail(loginUser.getUserId()));
    }

    /**
     * 获取KYC状态
     */
    @GetMapping("/status")
    @Operation(summary = "KYC状态", description = "获取当前用户的KYC认证状态")
    public ApiResponse<KYCDto.KYCStatusResponse> getKYCStatus(
            @AuthenticationPrincipal LoginUser loginUser) {

        KYCDto.KYCStatusResponse status = kycService.getKYCStatus(loginUser.getUserId());
        return ApiResponse.success(status);
    }

    /**
     * 获取KYC详情
     */
    @GetMapping("/detail")
    @Operation(summary = "KYC详情", description = "获取当前用户的KYC认证详情")
    public ApiResponse<KYCDto.KYCResponse> getKYCDetail(
            @AuthenticationPrincipal LoginUser loginUser) {

        KYCDto.KYCResponse detail = kycService.getKYCDetail(loginUser.getUserId());
        if (detail == null) {
            return ApiResponse.error("尚未提交KYC认证");
        }
        return ApiResponse.success(detail);
    }
}
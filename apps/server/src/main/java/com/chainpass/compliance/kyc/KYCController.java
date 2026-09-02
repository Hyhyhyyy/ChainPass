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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @GetMapping("/reviews")
    @PreAuthorize("hasAuthority('compliance:kyc:audit')")
    @Operation(summary = "审核队列", description = "状态：1待审、2通过、3拒绝；最多返回200条")
    public ApiResponse<List<KYCDto.KYCReviewResponse>> getReviews(
            @RequestParam(defaultValue = "1") Integer status) {
        return ApiResponse.success(kycService.getReviewQueue(status));
    }

    @GetMapping("/reviews/{id}")
    @PreAuthorize("hasAuthority('compliance:kyc:audit')")
    @Operation(summary = "审核材料详情", description = "包含敏感证件号码，仅授权审核员可访问")
    public ApiResponse<KYCDto.KYCReviewResponse> getReview(@PathVariable Long id) {
        return ApiResponse.success(kycService.getReviewSubmission(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('compliance:kyc:audit')")
    @Operation(summary = "审核通过KYC", description = "审核通过后签发KYC可验证凭证")
    public ApiResponse<Void> approveKYC(@AuthenticationPrincipal LoginUser reviewer, @PathVariable Long id) {
        kycService.approveKYC(id, reviewer.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('compliance:kyc:audit')")
    @Operation(summary = "拒绝KYC")
    public ApiResponse<Void> rejectKYC(@AuthenticationPrincipal LoginUser reviewer,
                                       @PathVariable Long id,
                                       @RequestParam String reason) {
        kycService.rejectKYC(id, reviewer.getUserId(), reason);
        return ApiResponse.success();
    }
}

package com.chainpass.vc.controller;

import com.chainpass.entity.LoginUser;
import com.chainpass.vc.dto.VCDto;
import com.chainpass.vc.entity.VCType;
import com.chainpass.vc.entity.VerifiableCredential;
import com.chainpass.vc.service.VCService;
import com.chainpass.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VC控制器 - 可验证凭证API
 */

@RestController
@RequestMapping("/vc")
@RequiredArgsConstructor
@Tag(name = "VC管理", description = "可验证凭证管理接口")
public class VCController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VCController.class);

    private final VCService vcService;
    private final com.chainpass.did.service.DIDService didService;

    /**
     * 验证凭证
     */
    @PostMapping("/verify")
    @Operation(summary = "验证凭证", description = "验证可验证凭证的有效性")
    public ApiResponse<VCDto.VerifyResult> verifyVC(@Valid @RequestBody VCDto.VerifyVCRequest request) {
        log.info("Verifying VC: {}", request.getVcId());
        VCDto.VerifyResult result = vcService.verifyCredential(request.getVcId());
        return ApiResponse.success(result);
    }

    /**
     * 获取我的凭证列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的凭证", description = "获取当前用户的所有可验证凭证")
    public ApiResponse<List<VCDto.VCResponse>> getMyVCs(@AuthenticationPrincipal LoginUser loginUser) {
        // 获取用户DID
        String did = getUserDID(loginUser.getUserId());
        if (did == null) {
            return ApiResponse.error("用户尚未创建DID");
        }

        List<VCDto.VCResponse> vcs = vcService.getVCListByHolder(did);
        return ApiResponse.success(vcs);
    }

    /**
     * 获取所有凭证类型
     */
    @GetMapping("/types")
    @Operation(summary = "获取凭证类型", description = "获取所有可用的凭证类型")
    public ApiResponse<List<VCType>> getVCTypes() {
        List<VCType> types = vcService.getAllVCTypes();
        return ApiResponse.success(types);
    }

    /**
     * 获取用户DID（辅助方法）
     */
    private String getUserDID(Long userId) {
        com.chainpass.did.entity.DIDDocument didDoc = didService.getDIDByUserId(userId);
        return didDoc != null ? didDoc.getId() : null;
    }
}

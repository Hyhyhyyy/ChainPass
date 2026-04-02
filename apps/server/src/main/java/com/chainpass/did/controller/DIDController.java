package com.chainpass.did.controller;

import com.chainpass.did.dto.*;
import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.service.DIDService;
import com.chainpass.entity.LoginUser;
import com.chainpass.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * DID控制器 - 去中心化身份API
 */

@RestController
@RequestMapping("/did")
@RequiredArgsConstructor
@Tag(name = "DID管理", description = "去中心化身份管理接口")
public class DIDController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DIDController.class);

    private final DIDService didService;

    /**
     * 创建DID
     */
    @PostMapping("/create")
    @Operation(summary = "创建DID", description = "为当前用户创建去中心化身份")
    public ApiResponse<DIDDocument> createDID(@AuthenticationPrincipal LoginUser loginUser) {
        log.info("Creating DID for user: {}", loginUser.getUserId());
        DIDDocument document = didService.createDID(loginUser.getUserId());
        return ApiResponse.success(document);
    }

    /**
     * 获取当前用户的DID
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的DID", description = "获取当前用户的DID信息")
    public ApiResponse<DIDResponse> getMyDID(@AuthenticationPrincipal LoginUser loginUser) {
        DIDResponse response = didService.getDIDResponse(loginUser.getUserId());
        if (response == null) {
            return ApiResponse.error("用户尚未创建DID");
        }
        return ApiResponse.success(response);
    }

    /**
     * 根据DID获取文档
     */
    @GetMapping("/{did}")
    @Operation(summary = "获取DID文档", description = "根据DID标识符获取DID文档")
    public ApiResponse<DIDDocument> getDID(@PathVariable String did) {
        DIDDocument document = didService.getDIDByDid(did);
        if (document == null) {
            return ApiResponse.error("DID不存在");
        }
        return ApiResponse.success(document);
    }

    /**
     * 验证DID签名
     */
    @PostMapping("/verify")
    @Operation(summary = "验证DID签名", description = "验证DID持有者的签名")
    public ApiResponse<Boolean> verifyDID(@Valid @RequestBody VerifyDIDRequest request) {
        log.info("Verifying DID: {}", request.getDid());
        boolean valid = didService.verifySignature(request);
        return ApiResponse.success(valid);
    }

    /**
     * 吊销DID
     */
    @PostMapping("/revoke")
    @Operation(summary = "吊销DID", description = "吊销指定的DID")
    public ApiResponse<Void> revokeDID(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam String did,
            @RequestParam(required = false, defaultValue = "用户主动吊销") String reason) {

        log.info("Revoking DID: {} by user: {}", did, loginUser.getUserId());
        didService.revokeDID(did, reason);
        return ApiResponse.success();
    }

    /**
     * 检查DID是否有效
     */
    @GetMapping("/check/{did}")
    @Operation(summary = "检查DID有效性", description = "检查DID是否存在且有效")
    public ApiResponse<Boolean> checkDID(@PathVariable String did) {
        boolean valid = didService.isValidDID(did);
        return ApiResponse.success(valid);
    }
}
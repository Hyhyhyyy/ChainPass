package com.chainpass.controller;

import com.chainpass.service.OAuthService;
import com.chainpass.vo.ApiResponse;
import com.chainpass.vo.OAuthConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * OAuth 控制器
 */

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuthController.class);

    private final OAuthService oauthService;

    /**
     * 获取 Gitee OAuth 配置
     */
    @PostMapping("/gitee/config")
    public ApiResponse<OAuthConfig> getGiteeConfig(
            @RequestHeader(value = "X-Gitee-Id", required = false) String giteeId
    ) {
        log.info("Gitee OAuth config request, giteeId: {}", giteeId);
        OAuthConfig config = oauthService.getGiteeConfig(giteeId);
        return ApiResponse.success(config);
    }

    /**
     * Gitee OAuth 回调
     */
    @GetMapping("/gitee/callback")
    public void giteeCallback(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        log.info("Gitee OAuth callback, code: {}", code);

        try {
            String redirectUrl = oauthService.handleGiteeCallback(code);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Gitee OAuth failed: ", e);
            response.sendRedirect("/auth/login?error=oauth_failed");
        }
    }
}
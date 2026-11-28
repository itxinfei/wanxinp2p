package com.xfjr.uaa.controller;

import com.xfjr.common.domain.RestResponse;
import com.xfjr.uaa.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登出端点（FR-129 AC4：已登出令牌访问资源返回 401）。
 *
 * <p>自助登出：持有效访问令牌即可登出。将访问令牌写入 Redis 黑名单（网关与资源服务器
 * 校验时据此拒绝），并移除可选的刷新令牌授权记录。
 *
 * <p>说明：访问令牌为无状态 JWT，登出依赖黑名单兜底；刷新令牌由授权服务持久化记录移除。
 */
@RestController
@Tag(name = "Logout", description = "登出：吊销访问/刷新令牌(FR-129)")
public class LogoutController {

    private final TokenBlacklistService tokenBlacklistService;
    private final OAuth2AuthorizationService authorizationService;

    public LogoutController(TokenBlacklistService tokenBlacklistService,
                            OAuth2AuthorizationService authorizationService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "登出（拉黑访问令牌并移除刷新令牌）")
    @PostMapping("/logout")
    public RestResponse<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenBlacklistService.revoke(authorization.substring(7));
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            OAuth2Authorization authorizationRecord = authorizationService.findByToken(
                    refreshToken, OAuth2TokenType.REFRESH_TOKEN);
            if (authorizationRecord != null) {
                authorizationService.remove(authorizationRecord);
            }
        }
        return RestResponse.success();
    }
}

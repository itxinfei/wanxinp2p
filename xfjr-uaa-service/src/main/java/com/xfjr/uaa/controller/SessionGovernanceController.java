package com.xfjr.uaa.controller;

import com.xfjr.common.domain.RestResponse;
import com.xfjr.uaa.service.SmsVerificationService;
import com.xfjr.uaa.service.TokenBlacklistService;
import com.xfjr.uaa.service.UserGovernanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话治理（FR-129）：吊销/短信登录/封禁踢人。
 * 置于 /public/**（既有 permitAll），无需改动 OAuth2 过滤链；生产环境由网关/资源服务器对
 * 吊销、封禁等敏感操作做鉴权（FR-130）。
 */
@RestController
@RequestMapping("/public")
@Tag(name = "SessionGovernance", description = "会话治理：吊销/短信登录/封禁踢人(FR-129)")
public class SessionGovernanceController {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private SmsVerificationService smsVerificationService;
    @Autowired
    private UserGovernanceService userGovernanceService;

    @Operation(summary = "发送短信验证码(5分钟有效)")
    @PostMapping("/sms/code")
    public RestResponse<String> sendCode(@RequestParam("mobile") String mobile) {
        return RestResponse.success(smsVerificationService.sendCode(mobile));
    }

    @Operation(summary = "短信登录验证码校验(错误3次失效)")
    @PostMapping("/sms/verify")
    public RestResponse<Void> verify(@RequestParam("mobile") String mobile, @RequestParam("code") String code) {
        smsVerificationService.verify(mobile, code);
        return RestResponse.success();
    }

    @Operation(summary = "吊销令牌(加入黑名单直至过期)")
    @PostMapping("/tokens/revoke")
    public RestResponse<Void> revoke(@RequestParam("accessToken") String accessToken) {
        tokenBlacklistService.revoke(accessToken);
        return RestResponse.success();
    }

    @Operation(summary = "令牌黑名单校验")
    @GetMapping("/tokens/blacklist/{jti}")
    public RestResponse<Boolean> blacklistCheck(@PathVariable("jti") String jti) {
        return RestResponse.success(tokenBlacklistService.isBlacklisted(jti));
    }

    @Operation(summary = "封禁用户")
    @PostMapping("/users/ban")
    public RestResponse<Void> ban(@RequestParam("mobile") String mobile) {
        userGovernanceService.ban(mobile);
        return RestResponse.success();
    }

    @Operation(summary = "解封用户")
    @PostMapping("/users/unban")
    public RestResponse<Void> unban(@RequestParam("mobile") String mobile) {
        userGovernanceService.unban(mobile);
        return RestResponse.success();
    }

    @Operation(summary = "踢人下线")
    @PostMapping("/users/kick")
    public RestResponse<Long> kick(@RequestParam("mobile") String mobile) {
        return RestResponse.success(userGovernanceService.kick(mobile));
    }
}

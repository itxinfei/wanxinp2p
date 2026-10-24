package com.xfjr.consumer.config;

import com.alibaba.fastjson2.JSON;
import com.xfjr.api.account.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 从 JWT 声明直接读取当前用户（FR-130 AC4），替代 jsonToken 透传机制。
 * 仅在启用 {@code oauth2.resource-server} 后 SecurityContext 中持有 {@link Jwt}；
 * 未启用时返回空 LoginUser（不抛错），与既有 SecurityUtil 行为兼容。
 */
public final class JwtUserContext {

    private JwtUserContext() {
    }

    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt)) {
            return new LoginUser();
        }
        Jwt jwt = (Jwt) auth.getPrincipal();
        LoginUser user = new LoginUser();
        String username = jwt.getClaimAsString("user_name");
        user.setUsername(username != null ? username : jwt.getSubject());
        user.setMobile(jwt.getClaimAsString("mobile"));
        user.setTenantId(jwt.getClaimAsString("tenant_id"));
        user.setDepartmentId(jwt.getClaimAsString("department_id"));
        user.setClientId(jwt.getClaimAsString("azp"));
        Object authorities = jwt.getClaim("user_authorities");
        user.setUserAuthorities(authorities == null ? null : JSON.toJSONString(authorities));
        Object payload = jwt.getClaim("payload");
        user.setPayload(payload == null ? null : JSON.toJSONString(payload));
        return user;
    }
}

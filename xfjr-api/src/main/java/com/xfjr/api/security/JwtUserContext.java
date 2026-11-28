package com.xfjr.api.security;

import com.alibaba.fastjson2.JSON;
import com.xfjr.api.account.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * JWT 用户上下文工具（FR-130 AC4）。
 *
 * <p>业务服务通过 OAuth2 Resource Server 本地校验令牌后，从 {@link SecurityContextHolder}
 * 中的 {@link Jwt} 直接读取统一用户声明，取代已废弃的 {@code jsonToken} query 参数机制（I-01）。
 *
 * <p>声明口径与认证中心签发保持一致：{@code user_name}、{@code mobile}、{@code tenant_id}、
 * {@code department_id}、{@code user_authorities}、{@code payload}、{@code azp}（客户端 ID）。
 */
public final class JwtUserContext {

    private JwtUserContext() {
    }

    /**
     * 获取当前登录用户。
     *
     * <p>未携带有效 JWT（如 permitAll 的内部端点）时返回空对象，保持与既有 SecurityUtil 一致的空值语义，
     * 具体端点的准入由 Spring Security 过滤链控制。
     *
     * @return 当前登录用户，未认证时为仅含空字段的对象
     */
    public static LoginUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return new LoginUser();
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        LoginUser loginUser = new LoginUser();
        String username = jwt.getClaimAsString("user_name");
        loginUser.setUsername(username != null ? username : jwt.getSubject());
        loginUser.setMobile(jwt.getClaimAsString("mobile"));
        loginUser.setTenantId(jwt.getClaimAsString("tenant_id"));
        loginUser.setDepartmentId(jwt.getClaimAsString("department_id"));
        Object userAuthorities = jwt.getClaim("user_authorities");
        loginUser.setUserAuthorities(userAuthorities == null ? null : JSON.toJSONString(userAuthorities));
        Object payload = jwt.getClaim("payload");
        loginUser.setPayload(payload == null ? null : JSON.toJSONString(payload));
        loginUser.setClientId(jwt.getClaimAsString("azp"));
        return loginUser;
    }
}

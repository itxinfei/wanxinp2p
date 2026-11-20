package com.xfjr.uaa.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码模式授权请求转换器。
 *
 * <p>将 POST /oauth2/token 中 grant_type=password 的请求转换为
 * {@link PasswordGrantAuthenticationToken}，交由
 * {@link PasswordGrantAuthenticationProvider} 认证。
 *
 * <p>客户端主体由前置的客户端认证过滤器写入 SecurityContext，与授权服务器内置的
 * 其他授权类型转换器保持一致。
 *
 * @author itxinfei
 * @since 2026-10-22
 */
public class PasswordGrantAuthenticationConverter implements AuthenticationConverter {

    /**
     * 密码模式授权类型
     */
    public static final AuthorizationGrantType PASSWORD_GRANT_TYPE = new AuthorizationGrantType(PasswordGrantAuthenticationToken.GRANT_TYPE);

    /**
     * 用户名字段名
     */
    private static final String USERNAME = "username";

    /**
     * 密码字段名
     */
    private static final String PASSWORD = "password";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 仅处理密码模式请求，其余授权类型交由内置转换器处理
        if (!PasswordGrantAuthenticationToken.GRANT_TYPE.equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))) {
            return null;
        }

        // 客户端主体（由客户端认证过滤器前置写入）
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 域名与认证类型由前端随令牌请求传入，用于选择账户域（b/c 端）与认证方式
        Map<String, Object> additionalParameters = new HashMap<>(2);
        additionalParameters.put("domain", request.getParameter("domain"));
        additionalParameters.put("authenticationType", request.getParameter("authenticationType"));

        return new PasswordGrantAuthenticationToken(
                PASSWORD_GRANT_TYPE,
                clientPrincipal,
                request.getParameter(USERNAME),
                request.getParameter(PASSWORD),
                additionalParameters);
    }
}

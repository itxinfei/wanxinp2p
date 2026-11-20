package com.xfjr.uaa.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Objects;

/**
 * 密码模式（Resource Owner Password Credentials Grant）授权请求令牌。
 *
 * <p>说明：Spring Authorization Server 1.x 依据 OAuth 2.1 已移除内置的 password 授权类型，
 * 而本项目管理后台（xfjr-web-admin）与移动端（xfjr-web-h5）仍使用该模式直接换取令牌，
 * 因此通过授权服务器的自定义扩展点（accessTokenRequestConverter + authenticationProvider）实现。
 *
 * @author itxinfei
 * @since 2026-10-22
 */
public class PasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 密码模式授权类型标识
     */
    public static final String GRANT_TYPE = "password";

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（手机号）
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;

    /**
     * 构造未认证的密码模式授权请求令牌
     *
     * @param authorizationGrantType 授权类型
     * @param clientPrincipal        已认证的客户端主体
     * @param username               用户名
     * @param password               密码
     * @param additionalParameters   附加参数（domain、authenticationType）
     */
    public PasswordGrantAuthenticationToken(AuthorizationGrantType authorizationGrantType,
                                            Authentication clientPrincipal,
                                            String username,
                                            String password,
                                            Map<String, Object> additionalParameters) {
        super(authorizationGrantType, clientPrincipal, additionalParameters);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        PasswordGrantAuthenticationToken that = (PasswordGrantAuthenticationToken) object;
        return Objects.equals(this.username, that.username) && Objects.equals(this.password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.username, this.password);
    }
}

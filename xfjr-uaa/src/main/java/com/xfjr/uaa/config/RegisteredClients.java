package com.xfjr.uaa.config;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.List;

/**
 * OAuth2 客户端清单（FR-129 AC1/AC2）。
 *
 * <p>客户端定义集中在此，供 {@code RegisteredClientRepository} 初始化种子与
 * {@link AuthorizationServer} 装配共用，避免多处维护同一份口径。
 *
 * <p>密钥一律以 {@code {bcrypt}} 前缀存储，明文只存在于本常量与部署文档，
 * 数据库中落库为 BCrypt 密文，无明文（AC2）。
 */
public final class RegisteredClients {

    /** 管理后台客户端 ID。 */
    public static final String ADMIN_CLIENT_ID = "xfjr-web-admin";
    /** 移动端客户端 ID。 */
    public static final String H5_CLIENT_ID = "xfjr-web-h5";
    /** 内部服务客户端 ID。 */
    public static final String INTERNAL_CLIENT_ID = "xfjr-internal";

    private static final String ADMIN_SECRET = "{bcrypt}$2b$10$b56flFH.7hWBT06RnVFij.5KKKE.kLUNkPxOTyCdr6JhHVdxBUmuW";
    private static final String H5_SECRET = "{bcrypt}$2b$10$.6hdRaq59awuQqmp3T7xAuwRWWrrWgL.k3c6HFkZ8Wgjsnj.hdJl.";
    private static final String INTERNAL_SECRET = "{bcrypt}$2b$10$4mdS9U1kDjcnqsZ4.1VDUuiNP80saQrXBe8LS3WuDpjpAPzOWoOmC";

    private static final List<String> REDIRECT_URIS = List.of(
            "http://localhost:8080/login", "http://127.0.0.1:8080/login");

    private RegisteredClients() {
    }

    /**
     * 令牌有效期口径：访问令牌 2 小时，刷新令牌 3 天（与 FR-129 现状一致）。
     */
    public static TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(7200))
                .refreshTokenTimeToLive(Duration.ofSeconds(259200))
                .build();
    }

    /**
     * 全部客户端。
     */
    public static List<RegisteredClient> all() {
        return List.of(adminClient(), h5Client(), internalClient());
    }

    /**
     * 管理后台客户端（密码模式直接换令牌，兼容存量前端）。
     */
    public static RegisteredClient adminClient() {
        return base(ADMIN_CLIENT_ID, ADMIN_SECRET)
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .authorizationGrantType(AuthorizationServer.PASSWORD_GRANT_TYPE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
    }

    /**
     * 移动端客户端（密码模式直接换令牌，兼容存量 H5）。
     */
    public static RegisteredClient h5Client() {
        return base(H5_CLIENT_ID, H5_SECRET)
                .scope("read")
                .scope("write")
                .authorizationGrantType(AuthorizationServer.PASSWORD_GRANT_TYPE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
    }

    /**
     * 内部服务客户端（服务间调用，客户端凭证模式）。
     */
    public static RegisteredClient internalClient() {
        return base(INTERNAL_CLIENT_ID, INTERNAL_SECRET)
                .scope("read")
                .scope("write")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
    }

    private static RegisteredClient.Builder base(String clientId, String clientSecret) {
        RegisteredClient.Builder builder = RegisteredClient.withId(clientId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .tokenSettings(tokenSettings());
        if (!INTERNAL_CLIENT_ID.equals(clientId)) {
            REDIRECT_URIS.forEach(builder::redirectUri);
        }
        return builder;
    }
}

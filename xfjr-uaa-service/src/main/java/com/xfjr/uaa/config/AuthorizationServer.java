package com.xfjr.uaa.config;

import com.xfjr.uaa.domain.UnifiedUserDetails;
import com.xfjr.uaa.oauth.PasswordGrantAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.time.Duration;

/**
 * Spring Authorization Server 配置（替代 spring-security-oauth2 的 @EnableAuthorizationServer）
 */
@Configuration
public class AuthorizationServer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 密码模式授权类型（OAuth 2.1 已废弃，Spring Authorization Server 1.x 未内置，
     * 本项目通过自定义扩展点实现，见 com.xfjr.uaa.oauth 包）。
     */
    public static final AuthorizationGrantType PASSWORD_GRANT_TYPE =
            PasswordGrantAuthenticationConverter.PASSWORD_GRANT_TYPE;

    /**
     * 注册客户端。客户端清单与 docs/uaa.sql、docs/p2p_sql/xfjr-init.sql 中的
     * oauth_client_details 种子数据保持一致；密钥待 TR-01 整改为 BCrypt 加密存储。
     *
     * @return 客户端仓库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // 管理后台客户端（Vue3 管理端，走密码模式直接换令牌）
        RegisteredClient adminClient = RegisteredClient.withId("xfjr-web-admin")
                .clientId("xfjr-web-admin")
                .clientSecret("{noop}itcastb")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(PASSWORD_GRANT_TYPE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login")
                .redirectUri("http://127.0.0.1:8080/login")
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .tokenSettings(tokenSettings())
                .build();
        // 移动端客户端（uni-app H5，走密码模式直接换令牌）
        RegisteredClient h5Client = RegisteredClient.withId("xfjr-web-h5")
                .clientId("xfjr-web-h5")
                .clientSecret("{noop}itcasth5")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(PASSWORD_GRANT_TYPE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login")
                .redirectUri("http://127.0.0.1:8080/login")
                .scope("read")
                .scope("write")
                .tokenSettings(tokenSettings())
                .build();
        // 内部服务客户端（服务间调用，走客户端凭证模式）
        RegisteredClient internalClient = RegisteredClient.withId("xfjr-internal")
                .clientId("xfjr-internal")
                .clientSecret("{noop}xfjr-internal-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("read")
                .scope("write")
                .tokenSettings(tokenSettings())
                .build();
        return new InMemoryRegisteredClientRepository(adminClient, h5Client, internalClient);
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(7200))   // 访问令牌默认有效期2小时
                .refreshTokenTimeToLive(Duration.ofSeconds(259200)) // 刷新令牌默认有效期3天
                .build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:53020")
                .build();
    }

    /**
     * 将统一用户信息（手机号、租户、部门、权限等）写入 JWT，供资源服务器透传
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication principal = context.getPrincipal();
                if (principal != null && principal.getPrincipal() instanceof UnifiedUserDetails) {
                    UnifiedUserDetails user = (UnifiedUserDetails) principal.getPrincipal();
                    context.getClaims().claims(claims -> {
                        claims.put("mobile", user.getMobile());
                        claims.put("tenant_id", user.getTenantId());
                        claims.put("department_id", user.getDepartmentId());
                        claims.put("user_authorities", user.getUserAuthorities());
                        claims.put("payload", user.getPayload());
                    });
                }
            }
        };
    }
}

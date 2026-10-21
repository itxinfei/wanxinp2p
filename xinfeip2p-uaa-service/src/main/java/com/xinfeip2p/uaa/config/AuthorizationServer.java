package com.xinfeip2p.uaa.config;

import com.xinfeip2p.uaa.domain.UnifiedUserDetails;
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
import java.util.UUID;

/**
 * Spring Authorization Server 配置（替代 spring-security-oauth2 的 @EnableAuthorizationServer）
 */
@Configuration
public class AuthorizationServer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // 网页端客户端（授权码模式）
        RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("xinfei-client")
                .clientSecret("{noop}xinfei-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login")
                .redirectUri("http://127.0.0.1:8080/login")
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .tokenSettings(tokenSettings())
                .build();
        // 密码模式客户端（供移动端/后台使用）
        // 注意：Spring Authorization Server 1.x 起移除了 password 授权类型（OAuth 2.1 已废弃该模式），
        // 该客户端改用 client_credentials + refresh_token 获取访问令牌。
        RegisteredClient passwordClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("xinfei-password")
                .clientSecret("{noop}xinfei-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .scope("write")
                .tokenSettings(tokenSettings())
                .build();
        return new InMemoryRegisteredClientRepository(webClient, passwordClient);
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

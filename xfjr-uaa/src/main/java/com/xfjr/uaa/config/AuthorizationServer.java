package com.xfjr.uaa.config;

import com.xfjr.uaa.domain.UnifiedUserDetails;
import com.xfjr.uaa.oauth.PasswordGrantAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * Spring Authorization Server 配置（替代 spring-security-oauth2 的 @EnableAuthorizationServer）。
 *
 * <p>客户端存储改为 JDBC（FR-129 AC1），密钥以 BCrypt 密文落库（AC2）；
 * 客户端清单统一定义在 {@link RegisteredClients}，初始数据由
 * {@link RegisteredClientSeedRunner} 启动时写入。
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
     * 注册客户端仓库：JDBC 实现，存储于 {@code oauth2_registered_client}（SAS 1.5 标准表结构）。
     *
     * @param jdbcTemplate JDBC 模板（数据源由 Apollo 命名空间 micro_service.spring-boot-druid 提供）
     * @return 客户端仓库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public TokenSettings tokenSettings() {
        return RegisteredClients.tokenSettings();
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

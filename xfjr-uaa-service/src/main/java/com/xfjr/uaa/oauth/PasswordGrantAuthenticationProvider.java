package com.xfjr.uaa.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 密码模式授权提供者。
 *
 * <p>职责：
 * <ol>
 *   <li>校验客户端已被认证且注册了密码模式授权类型；</li>
 *   <li>复用 {@code IntegrationUserDetailsAuthenticationProvider} 完成账户密码校验
 *       （domain / authenticationType 作为认证详情透传，与原密码模式行为保持一致）；</li>
 *   <li>生成访问令牌与刷新令牌并持久化授权记录，返回令牌认证结果。</li>
 * </ol>
 *
 * <p>安全说明：密码模式仅用于存量前端（管理后台、移动端）的兼容接入，
 * 新增客户端应改用授权码 + PKCE；详见需求规格说明书 FR-023。
 *
 * @author itxinfei
 * @since 2026-10-22
 */
public class PasswordGrantAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(PasswordGrantAuthenticationProvider.class);

    /**
     * 统一用户认证提供者（复用既有账户密码校验逻辑）
     */
    private final AuthenticationProvider userAuthenticationProvider;

    /**
     * 令牌生成器（由 Spring Boot 自动装配提供）
     */
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * 授权记录服务（由 Spring Boot 自动装配提供）
     */
    private final OAuth2AuthorizationService authorizationService;

    @Autowired
    public PasswordGrantAuthenticationProvider(AuthenticationProvider userAuthenticationProvider,
                                               OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                               OAuth2AuthorizationService authorizationService) {
        this.userAuthenticationProvider = userAuthenticationProvider;
        this.tokenGenerator = tokenGenerator;
        this.authorizationService = authorizationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordGrantAuthenticationToken token = (PasswordGrantAuthenticationToken) authentication;

        // 1. 客户端认证状态与授权类型校验
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientPrincipal(token);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null
                || !registeredClient.getAuthorizationGrantTypes()
                .contains(PasswordGrantAuthenticationConverter.PASSWORD_GRANT_TYPE)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, "客户端未注册密码模式授权类型", null));
        }

        // 2. 请求参数校验
        if (!StringUtils.hasText(token.getUsername()) || token.getPassword() == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_REQUEST, "用户名或密码为空", null));
        }

        // 3. 用户认证：复用统一账户认证提供者，domain/authenticationType 作为认证详情
        UsernamePasswordAuthenticationToken userRequest =
                new UsernamePasswordAuthenticationToken(token.getUsername(), token.getPassword());
        userRequest.setDetails(toUserDetails(token.getAdditionalParameters()));
        Authentication userAuthentication;
        try {
            userAuthentication = this.userAuthenticationProvider.authenticate(userRequest);
        } catch (AuthenticationException ex) {
            log.warn("密码模式认证失败, clientId={}, username={}",
                    registeredClient.getClientId(), token.getUsername());
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_GRANT, "用户名或密码错误", null));
        }

        // 4. 构建授权记录：默认授予客户端已注册的全部 scope
        Set<String> authorizedScopes = Collections.unmodifiableSet(
                new LinkedHashSet<>(registeredClient.getScopes()));
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization
                .withRegisteredClient(registeredClient)
                .principalName(userAuthentication.getName())
                .authorizationGrantType(PasswordGrantAuthenticationConverter.PASSWORD_GRANT_TYPE)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userAuthentication);

        // 5. 生成访问令牌
        OAuth2AccessToken accessToken = (OAuth2AccessToken) generateToken(registeredClient, userAuthentication,
                authorizedScopes, OAuth2TokenType.ACCESS_TOKEN, authorizationBuilder.build());
        if (accessToken == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR, "访问令牌生成失败", null));
        }
        authorizationBuilder.accessToken(accessToken);

        // 6. 生成刷新令牌（客户端注册了 refresh_token 授权类型时）
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            refreshToken = (OAuth2RefreshToken) generateToken(registeredClient, userAuthentication,
                    authorizedScopes, OAuth2TokenType.REFRESH_TOKEN, authorizationBuilder.build());
            if (refreshToken != null) {
                authorizationBuilder.refreshToken(refreshToken);
            }
        }

        // 7. 持久化授权记录
        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        log.info("密码模式授权成功, clientId={}, username={}",
                registeredClient.getClientId(), userAuthentication.getName());

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, userAuthentication, accessToken, refreshToken, Collections.emptyMap());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2AuthorizationGrantAuthenticationToken.class.isAssignableFrom(authentication)
                && PasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 生成指定类型的令牌
     *
     * @param registeredClient   注册客户端
     * @param userAuthentication 已认证的用户主体
     * @param authorizedScopes   已授权 scope
     * @param tokenType          令牌类型（访问令牌 / 刷新令牌）
     * @param authorization      当前授权记录
     * @return 生成的令牌，无法生成时返回 {@code null}
     */
    private OAuth2Token generateToken(RegisteredClient registeredClient,
                                      Authentication userAuthentication,
                                      Set<String> authorizedScopes,
                                      OAuth2TokenType tokenType,
                                      OAuth2Authorization authorization) {
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userAuthentication)
                .authorization(authorization)
                .authorizedScopes(authorizedScopes)
                .tokenType(tokenType)
                .authorizationGrantType(PasswordGrantAuthenticationConverter.PASSWORD_GRANT_TYPE)
                .build();
        return this.tokenGenerator.generate(tokenContext);
    }

    /**
     * 获取已认证的客户端主体
     *
     * @param token 密码模式授权请求令牌
     * @return 已认证的客户端认证令牌
     */
    private OAuth2ClientAuthenticationToken getAuthenticatedClientPrincipal(PasswordGrantAuthenticationToken token) {
        Authentication clientPrincipal = (Authentication) token.getPrincipal();
        if (clientPrincipal instanceof OAuth2ClientAuthenticationToken) {
            OAuth2ClientAuthenticationToken clientAuthentication =
                    (OAuth2ClientAuthenticationToken) clientPrincipal;
            if (clientAuthentication.isAuthenticated()) {
                return clientAuthentication;
            }
        }
        throw new OAuth2AuthenticationException(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_CLIENT, "客户端认证失败", null));
    }

    /**
     * 将令牌请求的附加参数转换为统一认证提供者所需的用户详情
     *
     * @param additionalParameters 附加参数
     * @return 包含 domain 与 authenticationType 的详情映射
     */
    private Map<String, String> toUserDetails(Map<String, Object> additionalParameters) {
        Map<String, String> details = new HashMap<>(2);
        if (additionalParameters != null) {
            details.put("domain", toText(additionalParameters.get("domain")));
            details.put("authenticationType", toText(additionalParameters.get("authenticationType")));
        }
        return details;
    }

    /**
     * 将对象转换为非空文本
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private String toText(Object value) {
        return value == null ? "" : value.toString();
    }
}

package com.xfjr.transaction.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 交易中心安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期），用户上下文由 {@code com.xfjr.api.security.JwtUserContext}
 * 从令牌声明还原，不信任任何透传的用户信息。默认启用；回滚置
 * {@code oauth2.resource-server.enabled=false}。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransactionSecurityConfig {

    @Bean
    public SecurityFilterChain transactionFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/l/**", "/public/**", "/webjars/**", "/v2/**",
                                "/swagger**", "/static/**", "/druid/**", "/actuator/**", "/error")
                        .permitAll()
                        // 面向登录用户的端点需具备读权限；标的查询类端点统一要求已认证
                        .requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

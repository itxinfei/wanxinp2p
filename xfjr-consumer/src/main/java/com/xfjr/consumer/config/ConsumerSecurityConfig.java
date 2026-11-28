package com.xfjr.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 用户中心安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期），不信任任何透传的用户信息，实现"每个服务独立校验、
 * 杜绝下游裸奔"。用户上下文由 {@code com.xfjr.api.security.JwtUserContext} 从令牌声明还原。
 *
 * <p>默认启用；需要回滚到旧机制时置 {@code oauth2.resource-server.enabled=false}。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConsumerSecurityConfig {

    /**
     * 内部调用是否改用客户端凭证令牌（FR-130 AC5）；开启后 /l/** 收敛为需 SCOPE_read。
     */
    @Value("${xfjr.internal-client.enabled:false}")
    private boolean internalClientEnabled;

    @Bean
    public SecurityFilterChain consumerFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/public/**", "/webjars/**", "/v2/**",
                                    "/swagger**", "/static/**", "/druid/**", "/actuator/**", "/error")
                            .permitAll();
                    // /l/** 默认放行以兼容未启用内部凭证的环境；开启后由 Feign 拦截器携带令牌
                    if (internalClientEnabled) {
                        auth.requestMatchers("/l/**").hasAuthority("SCOPE_read");
                    } else {
                        auth.requestMatchers("/l/**").permitAll();
                    }
                    // /my/** 与 /m/** 面向登录用户，需具备读权限；其余业务端点需已认证
                    auth.requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read");
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

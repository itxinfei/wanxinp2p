package com.xfjr.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 用户中心安全过滤链（FR-130 服务级鉴权，加法式）。
 *
 * <p>默认 {@code oauth2.resource-server.enabled=false}：放行所有请求，保持既有
 * jsonToken/SecurityUtil 机制，由网关统一鉴权（行为与改动前一致，不破坏存量端点）。
 *
 * <p>启用后：本地校验 JWT（签名 + 有效期），不信任任何透传的用户信息，实现
 * "每个服务独立校验、杜绝下游裸奔"（AC2/AC3/AC4）。启用需在配置中提供
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri} 或 {@code jwk-set-uri}。
 */
@Configuration
@EnableWebSecurity
public class ConsumerSecurityConfig {

    @Value("${oauth2.resource-server.enabled:false}")
    private boolean resourceServerEnabled;

    @Bean
    public SecurityFilterChain consumerFilterChain(HttpSecurity http) throws Exception {
        if (resourceServerEnabled) {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/l/**", "/public/**", "/webjars/**", "/v2/**",
                                    "/swagger**", "/static/**", "/druid/**", "/actuator/**", "/error")
                            .permitAll()
                            // /my/** 与 /api/** 需具备读权限；其余业务端点需已认证
                            .requestMatchers("/my/**", "/api/**").hasAuthority("SCOPE_read")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        } else {
            // 默认：不启用本地 JWT 校验，放行（网关统一鉴权 + 既有 jsonToken 机制）
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(csrf -> csrf.disable());
        }
        return http.build();
    }
}

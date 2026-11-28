package com.xfjr.repayment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 还款域安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期），用户上下文由 {@code com.xfjr.api.security.JwtUserContext}
 * 从令牌声明还原。默认启用；回滚置 {@code oauth2.resource-server.enabled=false}。
 *
 * <p>内部触发端点（{@code /l/**} 还款发起、{@code /execute-repayment/**} 调度执行、
 * {@code /repayment-notify/**} 异步通知回调）维持 permitAll 以兼容现有链路；
 * 这些端点的准入后续应由客户端凭证令牌收口（FR-130 AC5），见遗留清单。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RepaymentSecurityConfig {

    @Bean
    public SecurityFilterChain repaymentFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/l/**", "/public/**", "/execute-repayment/**",
                                "/repayment-notify/**", "/webjars/**", "/v2/**",
                                "/swagger**", "/static/**", "/druid/**", "/actuator/**", "/error")
                        .permitAll()
                        .requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

package com.xfjr.risk.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 风控服务安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期）；端点均为服务间调用的 {@code /l/**}，
 * 维持 permitAll 以兼容内部调用。默认启用；回滚置
 * {@code oauth2.resource-server.enabled=false}。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RiskSecurityConfig {

    @Bean
    public SecurityFilterChain riskFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/l/**", "/public/**", "/webjars/**", "/v2/**",
                                "/swagger**", "/static/**", "/druid/**", "/actuator/**", "/error")
                        .permitAll()
                        .requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

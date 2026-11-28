package com.xfjr.account.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 账户域安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期），不信任任何透传的用户信息。短信验证码与登录会话端点
 * 属匿名前置流程，维持 permitAll；其余端点需已认证。默认启用；回滚置
 * {@code oauth2.resource-server.enabled=false}。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccountSecurityConfig {

    @Bean
    public SecurityFilterChain accountFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // /sms/**、/mobiles/**：发送与校验短信验证码（登录前置，匿名）
                        .requestMatchers("/l/**", "/public/**", "/sms/**", "/mobiles/**",
                                "/webjars/**", "/v2/**", "/swagger**", "/static/**",
                                "/druid/**", "/actuator/**", "/error")
                        .permitAll()
                        .requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

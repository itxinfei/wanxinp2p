package com.xfjr.message.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 消息服务安全过滤链（FR-130 服务级鉴权）。
 *
 * <p>本地校验 JWT（签名 + 有效期）；端点均为服务间调用的 {@code /l/**}，
 * 维持 permitAll 以兼容内部调用。默认启用；回滚置
 * {@code oauth2.resource-server.enabled=false}。
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "oauth2.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageSecurityConfig {

    /**
     * 内部调用是否改用客户端凭证令牌（FR-130 AC5）；开启后 /l/** 收敛为需 SCOPE_read。
     */
    @Value("${xfjr.internal-client.enabled:false}")
    private boolean internalClientEnabled;

    @Bean
    public SecurityFilterChain messageFilterChain(HttpSecurity http) throws Exception {
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
                    auth.requestMatchers("/my/**", "/m/**", "/api/**").hasAuthority("SCOPE_read");
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

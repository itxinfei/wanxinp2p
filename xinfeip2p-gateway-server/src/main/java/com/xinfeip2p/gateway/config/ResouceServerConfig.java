package com.xinfeip2p.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关资源服务器配置（响应式 OAuth2 Resource Server，替代 spring-security-oauth2 的 @EnableResourceServer）：
 * 校验 JWT 并按路径进行访问控制。
 */
@Configuration
public class ResouceServerConfig {

    public static final String RESOURCE_ID = "xinfei-resource";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/uaa/druid/**").denyAll()
                .pathMatchers("/uaa/**").permitAll()
                .pathMatchers("/consumer/l/**").denyAll()
                .pathMatchers("/consumer/my/**").hasAuthority("SCOPE_read")
                .pathMatchers("/consumer/m/**").hasAuthority("SCOPE_read")
                .pathMatchers("/consumer/**").permitAll()
                .anyExchange().authenticated()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}

package com.xinfeip2p.uaa.config;

import com.xinfeip2p.uaa.domain.IntegrationUserDetailsAuthenticationHandler;
import com.xinfeip2p.uaa.domain.IntegrationUserDetailsAuthenticationProvider;
import com.xinfeip2p.uaa.domain.IntegrationWebAuthenticationDetailsSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Spring Security 过滤链配置（替代 WebSecurityConfigurerAdapter），
 * 集成 Spring Authorization Server 端点与自定义登录认证。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public IntegrationUserDetailsAuthenticationProvider integrationUserDetailsAuthenticationProvider() {
        return new IntegrationUserDetailsAuthenticationProvider(new IntegrationUserDetailsAuthenticationHandler());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   IntegrationWebAuthenticationDetailsSource detailsSource) throws Exception {
        // 注册自定义认证提供者（供表单登录与密码模式使用）
        http.authenticationProvider(integrationUserDetailsAuthenticationProvider());

        // 应用 Spring Authorization Server 端点（/oauth2/authorize、/oauth2/token、/oauth2/jwks 等）
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.apply(authorizationServerConfigurer);

        http.exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/login.do", "/public/**", "/webjars/**", "/v2/**",
                        "/swagger**", "/static/**", "/resources/**", "/druid/**", "/hi",
                        "/confirm_access", "/oauth_error").permitAll()
                .anyRequest().authenticated());

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login.do")
                .failureUrl("/login?authentication_error=1")
                .defaultSuccessUrl("/oauth2/authorize")
                .usernameParameter("username")
                .passwordParameter("password")
                .authenticationDetailsSource(detailsSource));

        http.logout(logout -> logout
                .logoutUrl("/logout.do")
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/"));

        return http.build();
    }
}

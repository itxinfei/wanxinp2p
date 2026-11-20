package com.xfjr.uaa.config;

import com.xfjr.uaa.domain.IntegrationUserDetailsAuthenticationHandler;
import com.xfjr.uaa.domain.IntegrationUserDetailsAuthenticationProvider;
import com.xfjr.uaa.domain.IntegrationWebAuthenticationDetailsSource;
import com.xfjr.uaa.oauth.PasswordGrantAuthenticationConverter;
import com.xfjr.uaa.oauth.PasswordGrantAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Spring Security 过滤链配置（替代 WebSecurityConfigurerAdapter），
 * 集成 Spring Authorization Server 端点与自定义登录认证。
 *
 * 注意：Spring Authorization Server 7.x 移除了 OAuth2AuthorizationServerConfigurer，
 * 届时需改为手动注册端点过滤器的双过滤链模式。当前使用 1.5.0（与 Security 7.x 编译兼容）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public IntegrationUserDetailsAuthenticationProvider integrationUserDetailsAuthenticationProvider() {
        return new IntegrationUserDetailsAuthenticationProvider(new IntegrationUserDetailsAuthenticationHandler());
    }

    /**
     * 密码模式授权提供者。
     *
     * <p>Spring Authorization Server 1.x 已移除内置的 password 授权类型（OAuth 2.1 废弃该模式），
     * 而管理后台与移动端仍使用该模式换令牌，故在此注册自定义实现以保持前后端可用。
     *
     * @param tokenGenerator                       令牌生成器（Spring Boot 自动装配）
     * @param authorizationService                 授权记录服务（Spring Boot 自动装配）
     * @param integrationUserDetailsAuthenticationProvider 统一用户认证提供者
     * @return 密码模式授权提供者
     */
    @Bean
    public PasswordGrantAuthenticationProvider passwordGrantAuthenticationProvider(
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            OAuth2AuthorizationService authorizationService,
            IntegrationUserDetailsAuthenticationProvider integrationUserDetailsAuthenticationProvider) {
        return new PasswordGrantAuthenticationProvider(
                integrationUserDetailsAuthenticationProvider, tokenGenerator, authorizationService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   IntegrationWebAuthenticationDetailsSource detailsSource,
                                                   PasswordGrantAuthenticationProvider passwordGrantAuthenticationProvider) throws Exception {
        // 注册自定义认证提供者（供表单登录与密码模式使用）
        http.authenticationProvider(integrationUserDetailsAuthenticationProvider());

        // 应用 Spring Authorization Server 端点（/oauth2/authorize、/oauth2/token、/oauth2/jwks 等）
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.apply(authorizationServerConfigurer);

        // 注册密码模式自定义授权：请求转换器 + 认证提供者
        // 注意：http.apply() 仅执行 init()，configure() 延迟至 http.build() 时执行，故此处注册仍生效
        authorizationServerConfigurer.tokenEndpoint(tokenEndpoint -> tokenEndpoint
                .accessTokenRequestConverter(new PasswordGrantAuthenticationConverter())
                .authenticationProvider(passwordGrantAuthenticationProvider));

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
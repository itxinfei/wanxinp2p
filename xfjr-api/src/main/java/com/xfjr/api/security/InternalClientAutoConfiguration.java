package com.xfjr.api.security;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 内部客户端凭证组件自动装配。
 *
 * <p>所有依赖 xfjr-api 的服务在引入 spring-cloud-openfeign 后自动获得
 * {@link FeignClientAuthInterceptor}，无需逐个服务声明 Bean；未配置
 * {@code xfjr.internal-client.client-secret} 时不产生任何行为变化。
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
@EnableConfigurationProperties(InternalClientProperties.class)
public class InternalClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClientCredentialsTokenProvider clientCredentialsTokenProvider(InternalClientProperties properties) {
        return new ClientCredentialsTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeignClientAuthInterceptor feignClientAuthInterceptor(InternalClientProperties properties,
                                                                 ClientCredentialsTokenProvider tokenProvider) {
        return new FeignClientAuthInterceptor(properties, tokenProvider);
    }
}

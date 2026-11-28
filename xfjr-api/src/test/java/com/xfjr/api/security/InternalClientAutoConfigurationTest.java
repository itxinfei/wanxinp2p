package com.xfjr.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内部客户端自动装配验证：引入 openfeign 时自动注册 Feign 拦截器与令牌提供者。
 */
class InternalClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(InternalClientAutoConfiguration.class));

    @Test
    void 应注册拦截器与令牌提供者() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(FeignClientAuthInterceptor.class);
            assertThat(context).hasSingleBean(ClientCredentialsTokenProvider.class);
            assertThat(context).hasSingleBean(InternalClientProperties.class);
        });
    }
}

package com.xfjr.api.seata;

import feign.RequestInterceptor;
import org.apache.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Seata 相关自动装配：仅当 classpath 存在 Seata（资源服务/调用方已引入 starter）时生效。
 */
@AutoConfiguration
@ConditionalOnClass({RequestInterceptor.class, RootContext.class})
public class SeataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SeataFeignInterceptor seataFeignInterceptor() {
        return new SeataFeignInterceptor();
    }
}

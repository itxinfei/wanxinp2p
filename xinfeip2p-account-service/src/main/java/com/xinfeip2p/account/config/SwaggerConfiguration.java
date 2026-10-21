package com.xinfeip2p.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi API 文档配置（替代 springfox）
 */
@Configuration
@ConditionalOnProperty(prefix = "swagger", value = {"enable"}, havingValue = "true")
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springDocOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("心飞金融P2P平台-用户服务API文档")
                .description("包含统一账户服务api")
                .version("1.0.0")
                .contact(new Contact().name("黑马程序员")));
    }
}

package com.xfjr.api.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Feign 调用自动注入内部客户端令牌（FR-130 AC5）。
 *
 * <p>仅当 {@code xfjr.internal-client.enabled=true} 且已配置 client-secret 时生效；
 * 否则保持旧行为（不注入头），便于未接入密钥的环境平滑回退。
 */
public class FeignClientAuthInterceptor implements RequestInterceptor {

    private final InternalClientProperties properties;

    private final ClientCredentialsTokenProvider tokenProvider;

    public FeignClientAuthInterceptor(InternalClientProperties properties,
                                      ClientCredentialsTokenProvider tokenProvider) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isEnabled()) {
            return;
        }
        String token = tokenProvider.getTokenOrEmpty();
        if (!token.isEmpty()) {
            template.header("Authorization", "Bearer " + token);
        }
    }
}

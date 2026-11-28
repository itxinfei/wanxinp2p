package com.xfjr.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 服务间内部调用（客户端凭证模式）配置。
 *
 * <p>对应 FR-130 AC5：内部 {@code /l/**} 调用改用 OAuth2 client_credentials 令牌，
 * 不再依赖"网关透传用户身份"。密钥只允许由 Apollo/KMS 注入，禁止写死在代码库。
 *
 * <p>开关 {@code enabled=true} 时，Feign 调用自动携带 internal 客户端令牌，
 * 且服务端会把 {@code /l/**} 从 permitAll 收敛为需 {@code SCOPE_read}。
 */
@ConfigurationProperties(prefix = "xfjr.internal-client")
public class InternalClientProperties {

    /**
     * 是否启用内部客户端凭证调用，默认关闭（保持 /l/** 放行，避免未配置密钥的环境整链路 401）。
     */
    private boolean enabled = false;

    /**
     * 令牌端点，默认指向本机 UAA。
     */
    private String tokenUri = "http://localhost:53020/oauth2/token";

    /**
     * 内部客户端 ID，与 xfjr-uaa RegisteredClients 中的 xfjr-internal 一致。
     */
    private String clientId = "xfjr-internal";

    /**
     * 客户端密钥（明文，仅用于换取令牌）。默认空，必须由 Apollo 注入；为空时拦截器不注入令牌。
     */
    private String clientSecret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}

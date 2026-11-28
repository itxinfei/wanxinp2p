package com.xfjr.api.security;

import com.sun.net.httpserver.HttpServer;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FR-130 AC5 内部客户端凭证组件测试：未启用不注入；启用后自动携带令牌且令牌按 TTL 缓存。
 */
class FeignClientAuthInterceptorTest {

    private HttpServer tokenServer;

    private String tokenUri;

    private final AtomicInteger tokenRequests = new AtomicInteger();

    @BeforeEach
    void startTokenServer() throws IOException {
        tokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        tokenServer.createContext("/oauth2/token", exchange -> {
            tokenRequests.incrementAndGet();
            byte[] body = "{\"access_token\":\"test-token\",\"token_type\":\"Bearer\",\"expires_in\":7200}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        tokenServer.start();
        tokenUri = "http://127.0.0.1:" + tokenServer.getAddress().getPort() + "/oauth2/token";
    }

    @AfterEach
    void stopTokenServer() {
        tokenServer.stop(0);
    }

    @Test
    void 启用后拦截器应注入令牌并且令牌被缓存() {
        InternalClientProperties properties = enabledProperties();
        ClientCredentialsTokenProvider provider = new ClientCredentialsTokenProvider(properties);
        FeignClientAuthInterceptor interceptor = new FeignClientAuthInterceptor(properties, provider);

        RequestTemplate first = new RequestTemplate();
        interceptor.apply(first);
        RequestTemplate second = new RequestTemplate();
        interceptor.apply(second);

        assertThat(first.headers()).containsEntry("Authorization", List.of("Bearer test-token"));
        assertThat(second.headers()).containsEntry("Authorization", List.of("Bearer test-token"));
        // 缓存生效：两次调用只换取一次令牌
        assertThat(tokenRequests).hasValue(1);
    }

    @Test
    void 未启用时不应发起令牌请求也不注入头() {
        InternalClientProperties properties = enabledProperties();
        properties.setEnabled(false);
        ClientCredentialsTokenProvider provider = new ClientCredentialsTokenProvider(properties);
        FeignClientAuthInterceptor interceptor = new FeignClientAuthInterceptor(properties, provider);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).isEmpty();
        assertThat(tokenRequests).hasValue(0);
    }

    @Test
    void 密钥为空时不应发起令牌请求() {
        InternalClientProperties properties = enabledProperties();
        properties.setClientSecret("");
        ClientCredentialsTokenProvider provider = new ClientCredentialsTokenProvider(properties);

        assertThat(provider.getTokenOrEmpty()).isEmpty();
        assertThat(tokenRequests).hasValue(0);
    }

    private InternalClientProperties enabledProperties() {
        InternalClientProperties properties = new InternalClientProperties();
        properties.setEnabled(true);
        properties.setTokenUri(tokenUri);
        properties.setClientId("xfjr-internal");
        properties.setClientSecret("secret");
        return properties;
    }
}

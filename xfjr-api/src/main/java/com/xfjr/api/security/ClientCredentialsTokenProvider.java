package com.xfjr.api.security;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * 客户端凭证令牌提供者：向 UAA 换取 client_credentials 访问令牌并本地缓存。
 *
 * <p>缓存到 expires_in 到期前 60 秒，避免边界时刻使用已过期令牌；并发调用通过
 * {@code synchronized} 串行化换取，防止重复请求令牌端点。
 */
public class ClientCredentialsTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsTokenProvider.class);

    /**
     * 提前刷新缓冲（秒），避免令牌在调用途中过期。
     */
    private static final long REFRESH_BUFFER_SECONDS = 60L;

    private final InternalClientProperties properties;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private volatile String cachedToken;

    private volatile Instant expireAt = Instant.EPOCH;

    public ClientCredentialsTokenProvider(InternalClientProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取可用的访问令牌；未启用或未配置密钥时返回空串（调用方据此跳过注入）。
     */
    public String getTokenOrEmpty() {
        if (!properties.isEnabled() || properties.getClientSecret() == null
                || properties.getClientSecret().isBlank()) {
            return "";
        }
        if (cachedToken != null && Instant.now().isBefore(expireAt)) {
            return cachedToken;
        }
        synchronized (this) {
            if (cachedToken != null && Instant.now().isBefore(expireAt)) {
                return cachedToken;
            }
            return refresh();
        }
    }

    private String refresh() {
        String body = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(properties.getClientId(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(properties.getClientSecret(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getTokenUri()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("获取内部客户端令牌失败，HTTP " + response.statusCode()
                        + ": " + response.body());
            }
            JSONObject json = JSON.parseObject(response.body());
            String token = json.getString("access_token");
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("令牌端点未返回 access_token: " + response.body());
            }
            long expiresIn = json.getLongValue("expires_in", 7200L);
            cachedToken = token;
            expireAt = Instant.now().plusSeconds(Math.max(expiresIn - REFRESH_BUFFER_SECONDS, 60));
            log.debug("内部客户端令牌已刷新，expires_in={}s", expiresIn);
            return token;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取内部客户端令牌被中断", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("获取内部客户端令牌异常", ex);
        }
    }
}

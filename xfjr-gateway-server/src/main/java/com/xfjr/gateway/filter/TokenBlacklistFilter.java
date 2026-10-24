package com.xfjr.gateway.filter;

import com.alibaba.fastjson2.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 令牌黑名单过滤（FR-130 AC1 已吊销）：在网关校验 JWT 签名/有效期之外，
 * 再查 Redis 黑名单（键 {@code uaa:token:blacklist:{jti}}，由 uaa 服务吊销时写入）。
 *
 * <p>加法式、默认关闭（{@code gateway.token-blacklist.enabled=true} 启用）；
 * 自行解码 Bearer 令牌取 jti（不依赖安全过滤链顺序），Redis 异常时 fail-open 保障可用性。
 */
@Component
@ConditionalOnProperty(prefix = "gateway.token-blacklist", name = "enabled", havingValue = "true")
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    private static final String KEY_PREFIX = "uaa:token:blacklist:";

    private final ReactiveStringRedisTemplate redis;

    public TokenBlacklistFilter(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        String jti = extractJti(header.substring(7));
        if (jti == null) {
            return chain.filter(exchange);
        }
        return redis.hasKey(KEY_PREFIX + jti)
                .defaultIfEmpty(Boolean.FALSE)
                .flatMap(blacklisted -> {
                    if (!Boolean.TRUE.equals(blacklisted)) {
                        return chain.filter(exchange);
                    }
                    return unauthorized(exchange, "令牌已吊销");
                })
                .onErrorResume(e -> chain.filter(exchange)); // Redis 异常 fail-open
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] body = ("{\"code\":401,\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    private String extractJti(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Object jti = JSON.parseObject(payload).get("jti");
            return jti == null ? null : String.valueOf(jti);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] base64UrlDecode(String s) {
        String padded = s;
        switch (padded.length() % 4) {
            case 2: padded += "=="; break;
            case 3: padded += "="; break;
            case 1: padded += "==="; break;
            default: break;
        }
        return Base64.getUrlDecoder().decode(padded);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

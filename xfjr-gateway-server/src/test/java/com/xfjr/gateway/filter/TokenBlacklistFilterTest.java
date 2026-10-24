package com.xfjr.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("网关令牌黑名单过滤（FR-130 AC1）")
class TokenBlacklistFilterTest {

    @Mock
    private ReactiveStringRedisTemplate redis;
    @Mock
    private GatewayFilterChain chain;
    @Mock
    private ServerWebExchange exchange;
    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpResponse response;
    @Mock
    private DataBufferFactory bufferFactory;
    @Mock
    private DataBuffer buffer;
    @InjectMocks
    private TokenBlacklistFilter filter;

    private String jwt(String jti) {
        String payload = "{\"jti\":\"" + jti + "\"}";
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return "header." + b64 + ".sig";
    }

    private HttpHeaders bearer(String jti) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt(jti));
        return headers;
    }

    @Test
    @DisplayName("无 Authorization 头 → 直接放行")
    void no_token_passthrough() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("已吊销令牌（黑名单命中）→ 401，不转发")
    void blacklisted_returns_401() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(bearer("abc123"));
        when(redis.hasKey("uaa:token:blacklist:abc123")).thenReturn(Mono.just(true));
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(bufferFactory);
        when(bufferFactory.wrap(any(byte[].class))).thenReturn(buffer);
        when(response.writeWith(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("有效令牌（未命中黑名单）→ 放行")
    void valid_token_passthrough() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(bearer("abc123"));
        when(redis.hasKey("uaa:token:blacklist:abc123")).thenReturn(Mono.just(false));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("Redis 异常 → fail-open 放行（保障可用性）")
    void redis_error_fail_open() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(bearer("abc123"));
        when(redis.hasKey("uaa:token:blacklist:abc123")).thenReturn(Mono.error(new RuntimeException("redis down")));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertTrue(true); // 未抛出即代表 fail-open 生效
        verify(chain).filter(exchange);
    }
}

package com.xfjr.uaa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.uaa.common.constant.UaaErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("令牌黑名单（FR-129）")
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;
    @InjectMocks
    private TokenBlacklistService service;

    private String jwt(String jti, long exp) {
        String payload = "{\"jti\":\"" + jti + "\",\"exp\":" + exp + "}";
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return "header." + b64 + ".sig";
    }

    @Test
    @DisplayName("吊销令牌 → 以 jti 为键写入黑名单直至过期")
    void revoke_writes_blacklist_by_jti() {
        long futureExp = System.currentTimeMillis() / 1000 + 3600;
        when(redis.opsForValue()).thenReturn(valueOps);

        service.revoke("Bearer " + jwt("abc123", futureExp));

        verify(valueOps).set(eq("uaa:token:blacklist:abc123"), eq("1"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("黑名单校验 → 命中/未命中")
    void isBlacklisted() {
        when(redis.hasKey("uaa:token:blacklist:abc123")).thenReturn(true);
        when(redis.hasKey("uaa:token:blacklist:other")).thenReturn(false);

        assertTrue(service.isBlacklisted("abc123"));
        assertFalse(service.isBlacklisted("other"));
    }

    @Test
    @DisplayName("解析 jti → 返回声明值")
    void jtiOf_returns_claim() {
        assertEquals("abc123", service.jtiOf(jwt("abc123", 123L)));
    }

    @Test
    @DisplayName("令牌缺少 jti → 抛 E_150201")
    void revoke_throws_when_no_jti() {
        String noJti = "header."
                + Base64.getUrlEncoder().withoutPadding().encodeToString("{\"exp\":123}".getBytes(StandardCharsets.UTF_8))
                + ".sig";

        BusinessException ex = assertThrows(BusinessException.class, () -> service.revoke(noJti));

        assertEquals(UaaErrorCode.E_150201, ex.getErrorCode());
    }
}

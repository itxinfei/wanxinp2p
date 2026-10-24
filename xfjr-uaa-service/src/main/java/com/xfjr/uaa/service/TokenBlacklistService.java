package com.xfjr.uaa.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.uaa.common.constant.UaaErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 令牌黑名单（FR-129 会话治理）：吊销令牌后加入 Redis 黑名单直至其过期。
 * 资源服务器（FR-130）据此判断已吊销/已登出令牌并返回 401（AC4）。
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "uaa:token:blacklist:";
    private static final long MAX_TTL_SECONDS = 2L * 24 * 3600;

    private final StringRedisTemplate redisTemplate;
    private final Gson gson = new Gson();

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 吊销令牌：解码 jti+exp，加入黑名单直至过期。 */
    public void revoke(String accessToken) {
        Map<String, Object> payload = decodePayload(accessToken);
        Object jti = payload.get("jti");
        if (jti == null) {
            throw new BusinessException(UaaErrorCode.E_150201, "令牌缺少 jti，无法吊销");
        }
        long ttl = expirySeconds(payload);
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttl, TimeUnit.SECONDS);
        log.info("令牌已吊销 jti={} ttl={}s", jti, ttl);
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }

    public String jtiOf(String accessToken) {
        Object jti = decodePayload(accessToken).get("jti");
        return jti == null ? null : String.valueOf(jti);
    }

    private long expirySeconds(Map<String, Object> payload) {
        Object exp = payload.get("exp");
        long expMs = (exp instanceof Number) ? ((Number) exp).longValue() * 1000L : System.currentTimeMillis();
        long ttl = (expMs - System.currentTimeMillis()) / 1000L;
        return Math.max(0, Math.min(ttl, MAX_TTL_SECONDS));
    }

    private Map<String, Object> decodePayload(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new BusinessException(UaaErrorCode.E_150401, "令牌不能为空");
        }
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new BusinessException(UaaErrorCode.E_150201, "令牌格式非法");
        }
        String payloadJson = base64UrlDecode(parts[1]);
        return gson.fromJson(payloadJson, new TypeToken<Map<String, Object>>() {}.getType());
    }

    private String base64UrlDecode(String s) {
        String padded = s;
        switch (padded.length() % 4) {
            case 2: padded += "=="; break;
            case 3: padded += "="; break;
            case 1: padded += "==="; break;
            default: break;
        }
        return new String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8);
    }
}

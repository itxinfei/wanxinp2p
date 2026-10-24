package com.xfjr.uaa.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("账号治理（FR-129）：封禁 / 解封 / 踢人下线")
class UserGovernanceServiceTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;
    @InjectMocks
    private UserGovernanceService service;

    @Test
    @DisplayName("封禁 → 写 Redis 标记")
    void ban_sets_key() {
        when(redis.opsForValue()).thenReturn(valueOps);

        service.ban("138");

        verify(valueOps).set("uaa:ban:138", "1");
    }

    @Test
    @DisplayName("解封 → 删除标记")
    void unban_deletes_key() {
        service.unban("138");

        verify(redis).delete("uaa:ban:138");
    }

    @Test
    @DisplayName("封禁状态查询 → 命中/未命中")
    void isBanned() {
        when(redis.hasKey("uaa:ban:138")).thenReturn(true);
        when(redis.hasKey("uaa:ban:000")).thenReturn(false);

        assertTrue(service.isBanned("138"));
        assertFalse(service.isBanned("000"));
    }

    @Test
    @DisplayName("踢人下线 → 记录时间戳；其后的会话视为被踢")
    void kick_and_isKicked() {
        when(redis.opsForValue()).thenReturn(valueOps);

        long t = service.kick("138");

        assertTrue(t > 0);
        when(valueOps.get("uaa:kick:138")).thenReturn(String.valueOf(t));

        assertTrue(service.isKicked("138", t - 1000));  // 踢人之前签发的令牌
        assertFalse(service.isKicked("138", t + 1000)); // 踢人之后签发的令牌（无，视为未踢）
    }
}

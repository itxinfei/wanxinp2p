package com.xfjr.uaa.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 账号治理（FR-129 会话治理）：封禁/解封/踢人下线，基于 Redis。
 */
@Service
public class UserGovernanceService {

    private static final String BAN_PREFIX = "uaa:ban:";
    private static final String KICK_PREFIX = "uaa:kick:";

    private final StringRedisTemplate redisTemplate;

    public UserGovernanceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void ban(String mobile) {
        redisTemplate.opsForValue().set(BAN_PREFIX + mobile, "1");
    }

    public void unban(String mobile) {
        redisTemplate.delete(BAN_PREFIX + mobile);
    }

    public boolean isBanned(String mobile) {
        return mobile != null && Boolean.TRUE.equals(redisTemplate.hasKey(BAN_PREFIX + mobile));
    }

    /** 踢人下线：记录时间戳，资源服务器据此拒绝该用户在此时间之后签发的令牌。返回时间戳。 */
    public long kick(String mobile) {
        long now = System.currentTimeMillis();
        redisTemplate.opsForValue().set(KICK_PREFIX + mobile, String.valueOf(now));
        return now;
    }

    public boolean isKicked(String mobile, long issuedAt) {
        if (mobile == null) {
            return false;
        }
        String v = redisTemplate.opsForValue().get(KICK_PREFIX + mobile);
        return v != null && Long.parseLong(v) > issuedAt;
    }
}

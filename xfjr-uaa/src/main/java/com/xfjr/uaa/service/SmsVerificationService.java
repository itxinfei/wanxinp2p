package com.xfjr.uaa.service;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.uaa.common.constant.UaaErrorCode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 短信登录验证码（FR-129 AC5）：5 分钟有效，错误 3 次失效。
 * 生产环境下发经短信网关；此处记录日志，验证码存 Redis。
 */
@Slf4j
@Service
public class SmsVerificationService {

    private static final String CODE_PREFIX = "uaa:sms:code:";
    private static final String FAIL_PREFIX = "uaa:sms:fail:";
    private static final long TTL_SECONDS = 300;
    private static final int MAX_ATTEMPTS = 3;

    private final StringRedisTemplate redisTemplate;

    public SmsVerificationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 生成并存储验证码（5 分钟有效）。 */
    public String sendCode(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new BusinessException(UaaErrorCode.E_150401, "手机号不能为空");
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        redisTemplate.opsForValue().set(CODE_PREFIX + mobile, code, TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.delete(FAIL_PREFIX + mobile);
        log.info("短信验证码已生成 mobile={}（生产环境经短信网关下发）", mask(mobile));
        return code;
    }

    /** 校验验证码；错误累计 3 次失效（AC5）。 */
    public void verify(String mobile, String code) {
        if (mobile == null || mobile.trim().isEmpty() || code == null) {
            throw new BusinessException(UaaErrorCode.E_150401, "参数不能为空");
        }
        String failStr = redisTemplate.opsForValue().get(FAIL_PREFIX + mobile);
        int fails = failStr == null ? 0 : Integer.parseInt(failStr);
        if (fails >= MAX_ATTEMPTS) {
            throw new BusinessException(UaaErrorCode.E_150102, "验证码错误次数过多已失效，请重新获取");
        }
        String stored = redisTemplate.opsForValue().get(CODE_PREFIX + mobile);
        if (stored == null) {
            throw new BusinessException(UaaErrorCode.E_150101, "验证码已过期，请重新获取");
        }
        if (stored.equals(code)) {
            redisTemplate.delete(CODE_PREFIX + mobile);
            redisTemplate.delete(FAIL_PREFIX + mobile);
            return;
        }
        int newFails = fails + 1;
        redisTemplate.opsForValue().set(FAIL_PREFIX + mobile, String.valueOf(newFails), TTL_SECONDS, TimeUnit.SECONDS);
        throw new BusinessException(UaaErrorCode.E_150101, "验证码错误（还可重试 " + (MAX_ATTEMPTS - newFails) + " 次）");
    }

    private String mask(String mobile) {
        if (mobile.length() < 7) {
            return "****";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}

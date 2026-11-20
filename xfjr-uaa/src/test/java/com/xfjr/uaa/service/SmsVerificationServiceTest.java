package com.xfjr.uaa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.uaa.common.constant.UaaErrorCode;
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
@DisplayName("短信登录验证码（FR-129 AC5）：5 分钟有效 + 错误 3 次失效")
class SmsVerificationServiceTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;
    @InjectMocks
    private SmsVerificationService service;

    @Test
    @DisplayName("校验通过 → 删除验证码与失败计数")
    void verify_success() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("uaa:sms:fail:138")).thenReturn(null);
        when(valueOps.get("uaa:sms:code:138")).thenReturn("123456");

        service.verify("138", "123456");

        verify(redis).delete("uaa:sms:code:138");
        verify(redis).delete("uaa:sms:fail:138");
    }

    @Test
    @DisplayName("校验错误 → 累计失败次数并抛 E_150101")
    void verify_wrong_increments_fail() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("uaa:sms:fail:138")).thenReturn(null);
        when(valueOps.get("uaa:sms:code:138")).thenReturn("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.verify("138", "000000"));

        assertEquals(UaaErrorCode.E_150101, ex.getErrorCode());
        verify(valueOps).set("uaa:sms:fail:138", "1", 300L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("错误次数达 3 → 失效，抛 E_150102")
    void verify_exhausted() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("uaa:sms:fail:138")).thenReturn("3");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.verify("138", "123456"));

        assertEquals(UaaErrorCode.E_150102, ex.getErrorCode());
    }

    @Test
    @DisplayName("验证码过期（无记录） → 抛 E_150101")
    void verify_expired() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("uaa:sms:fail:138")).thenReturn(null);
        when(valueOps.get("uaa:sms:code:138")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.verify("138", "123456"));

        assertEquals(UaaErrorCode.E_150101, ex.getErrorCode());
    }

    @Test
    @DisplayName("发送验证码 → 返回 6 位码并存 300 秒")
    void sendCode_returns_6_digit() {
        when(redis.opsForValue()).thenReturn(valueOps);

        String code = service.sendCode("138");

        assertEquals(6, code.length());
        verify(valueOps).set("uaa:sms:code:138", code, 300L, TimeUnit.SECONDS);
        verify(redis).delete("uaa:sms:fail:138");
    }
}

package com.xfjr.consumer.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.mapper.MemberPointRecordMapper;
import com.xfjr.consumer.member.entity.MemberPointRecord;
import com.xfjr.consumer.member.model.PointRecordDTO;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("积分（FR-127）")
class PointServiceTest {

    @Mock
    private MemberPointRecordMapper mapper;
    @InjectMocks
    private PointService service;

    private MemberPointRecord rec(String type, long amount) {
        MemberPointRecord r = new MemberPointRecord();
        r.setConsumerNo("U1");
        r.setPointType(type);
        r.setAmount(amount);
        return r;
    }

    @Test
    @DisplayName("发放积分 → 余额增加，流水为 EARN")
    void earn_should_add() {
        when(mapper.selectList(any())).thenReturn(Collections.emptyList());
        when(mapper.selectOne(any())).thenReturn(null);

        PointRecordDTO dto = service.earn("U1", "LOAN", 100L, "BIZ1", "出借奖励");

        assertEquals("EARN", dto.getPointType());
        assertEquals(100L, dto.getBalanceAfter().longValue());
        verify(mapper).insert(any(MemberPointRecord.class));
    }

    @Test
    @DisplayName("消耗积分 → 余额扣减")
    void consume_should_deduct() {
        when(mapper.selectList(any())).thenReturn(Collections.singletonList(rec("EARN", 100L)));
        when(mapper.selectOne(any())).thenReturn(null);

        PointRecordDTO dto = service.consume("U1", "DEDUCT", 50L, "BIZ2", "抵扣");

        assertEquals("CONSUME", dto.getPointType());
        assertEquals(50L, dto.getBalanceAfter().longValue());
    }

    @Test
    @DisplayName("余额不足 → 抛业务异常")
    void consume_should_throw_when_insufficient() {
        when(mapper.selectList(any())).thenReturn(Collections.singletonList(rec("EARN", 10L)));
        when(mapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.consume("U1", "DEDUCT", 50L, "BIZ2", "抵扣"));
    }

    @Test
    @DisplayName("同一 bizNo 幂等 → 不重复插入")
    void earn_should_be_idempotent() {
        MemberPointRecord existing = rec("EARN", 100L);
        existing.setBalanceAfter(100L);
        when(mapper.selectOne(any())).thenReturn(existing);

        PointRecordDTO dto = service.earn("U1", "LOAN", 100L, "BIZ1", "出借奖励");

        assertEquals(100L, dto.getBalanceAfter().longValue());
        verify(mapper, never()).insert(any(MemberPointRecord.class));
    }

    @Test
    @DisplayName("金额非正 → 抛业务异常")
    void earn_should_throw_when_nonpositive() {
        assertThrows(BusinessException.class, () -> service.earn("U1", "LOAN", 0L, "BIZ1", "r"));
    }

    @Test
    @DisplayName("余额 = 获取合计 - 消耗合计")
    void balance_should_net() {
        when(mapper.selectList(any())).thenReturn(Arrays.asList(rec("EARN", 100L), rec("CONSUME", 30L)));

        assertEquals(70L, service.currentBalance("U1"));
    }
}

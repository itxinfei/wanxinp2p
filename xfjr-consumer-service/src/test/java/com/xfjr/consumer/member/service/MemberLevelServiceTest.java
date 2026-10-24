package com.xfjr.consumer.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.common.domain.BusinessException;
import com.xfjr.consumer.mapper.MemberInfoMapper;
import com.xfjr.consumer.mapper.MemberLevelHistoryMapper;
import com.xfjr.consumer.mapper.MemberLevelMapper;
import com.xfjr.consumer.member.entity.MemberInfo;
import com.xfjr.consumer.member.entity.MemberLevelHistory;
import com.xfjr.consumer.member.entity.MemberLevel;
import com.xfjr.consumer.member.model.MemberInfoDTO;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("会员等级自动升级（FR-127）")
class MemberLevelServiceTest {

    @Mock
    private MemberInfoMapper infoMapper;
    @Mock
    private MemberLevelMapper levelMapper;
    @Mock
    private MemberLevelHistoryMapper historyMapper;
    @InjectMocks
    private MemberLevelService service;

    private MemberLevel lv(String code, int no, String min) {
        MemberLevel l = new MemberLevel();
        l.setLevelCode(code);
        l.setLevelNo(no);
        l.setMinLendAmount(new BigDecimal(min));
        l.setStatus(1);
        return l;
    }

    private void stubLevels() {
        when(levelMapper.selectList(any())).thenReturn(Arrays.asList(
                lv("BRONZE", 1, "0"), lv("SILVER", 2, "100000"), lv("GOLD", 3, "500000")));
    }

    @Test
    @DisplayName("累计出借达门槛 → 自动升级并记录历史")
    void recordLend_should_upgrade() {
        MemberInfo info = new MemberInfo();
        info.setId(1L);
        info.setConsumerNo("U1");
        info.setCurrentLevel("SILVER");
        info.setCumulativeLendAmount(new BigDecimal("490000"));
        info.setActiveScore(0);
        when(infoMapper.selectOne(any())).thenReturn(info);
        stubLevels();

        MemberInfoDTO dto = service.recordLend("U1", new BigDecimal("50000"));

        assertEquals("GOLD", dto.getCurrentLevel());
        assertEquals(0, new BigDecimal("540000").compareTo(dto.getCumulativeLendAmount()));
        verify(historyMapper).insert(any(MemberLevelHistory.class));
        verify(infoMapper).updateById(any(MemberInfo.class));
    }

    @Test
    @DisplayName("无档案 → 建档并升级")
    void recordLend_should_create_and_upgrade() {
        when(infoMapper.selectOne(any())).thenReturn(null);
        stubLevels();

        MemberInfoDTO dto = service.recordLend("U1", new BigDecimal("200000"));

        assertEquals("SILVER", dto.getCurrentLevel());
        verify(infoMapper).insert(any(MemberInfo.class));
        verify(historyMapper).insert(any(MemberLevelHistory.class));
    }

    @Test
    @DisplayName("出借金额非正 → 抛业务异常")
    void recordLend_should_throw_when_nonpositive() {
        assertThrows(BusinessException.class, () -> service.recordLend("U1", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("用户编码为空 → 抛业务异常")
    void recordLend_should_throw_when_blank() {
        assertThrows(BusinessException.class, () -> service.recordLend("  ", new BigDecimal("100")));
    }
}

package com.xfjr.statistics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xfjr.api.statistics.StatisticsOverviewDTO;
import com.xfjr.api.statistics.StatisticsSyncRequest;
import com.xfjr.statistics.entity.StatDailyOverview;
import com.xfjr.statistics.entity.StatRisk;
import com.xfjr.statistics.mapper.StatDailyOverviewMapper;
import com.xfjr.statistics.mapper.StatRiskMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("统计分析看板（FR-128）")
class DashboardServiceTest {

    @Mock
    private StatDailyOverviewMapper overviewMapper;
    @Mock
    private StatRiskMapper riskMapper;
    @InjectMocks
    private DashboardService service;

    @Test
    @DisplayName("经营看板 → 返回最新快照")
    void overview_should_return_latest() {
        StatDailyOverview ov = new StatDailyOverview();
        ov.setStatDate(LocalDate.of(2026, 10, 23));
        ov.setOutstandingBalance(new BigDecimal("1000000"));
        ov.setNewUserCount(5);
        when(overviewMapper.selectOne(any())).thenReturn(ov);

        StatisticsOverviewDTO dto = service.overview();

        assertNotNull(dto);
        assertEquals(5, dto.getNewUserCount().intValue());
    }

    @Test
    @DisplayName("无数据 → 返回 null")
    void overview_should_return_null_when_empty() {
        when(overviewMapper.selectOne(any())).thenReturn(null);
        assertNull(service.overview());
    }

    @Test
    @DisplayName("AC3 报表导出 → CSV 含表头与数据行")
    void exportOverview_should_build_csv() {
        StatDailyOverview o1 = new StatDailyOverview();
        o1.setStatDate(LocalDate.of(2026, 10, 22));
        o1.setOutstandingBalance(new BigDecimal("100"));
        o1.setNewUserCount(1);
        StatDailyOverview o2 = new StatDailyOverview();
        o2.setStatDate(LocalDate.of(2026, 10, 23));
        o2.setOutstandingBalance(new BigDecimal("200"));
        o2.setNewUserCount(2);
        when(overviewMapper.selectList(any())).thenReturn(Arrays.asList(o1, o2));

        String csv = service.exportOverview();

        assertTrue(csv.startsWith("statDate,"));
        assertTrue(csv.contains("2026-10-22"));
        assertTrue(csv.contains("2026-10-23"));
    }

    @Test
    @DisplayName("T+1 推送 → 无记录则插入两张表")
    void sync_should_insert_when_new() {
        when(overviewMapper.selectOne(any())).thenReturn(null);
        when(riskMapper.selectOne(any())).thenReturn(null);
        StatisticsSyncRequest req = new StatisticsSyncRequest();
        req.setStatDate(LocalDate.of(2026, 10, 23));
        req.setOutstandingBalance(new BigDecimal("100"));

        service.sync(req);

        verify(overviewMapper).insert(any(StatDailyOverview.class));
        verify(riskMapper).insert(any(StatRisk.class));
    }

    @Test
    @DisplayName("推送缺 statDate → 抛异常")
    void sync_should_throw_when_date_null() {
        StatisticsSyncRequest req = new StatisticsSyncRequest();
        assertThrows(IllegalArgumentException.class, () -> service.sync(req));
    }
}

package com.xfjr.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.statistics.StatisticsOverviewDTO;
import com.xfjr.api.statistics.StatisticsRiskDTO;
import com.xfjr.api.statistics.StatisticsSyncRequest;
import com.xfjr.statistics.entity.StatDailyOverview;
import com.xfjr.statistics.entity.StatRisk;
import com.xfjr.statistics.mapper.StatDailyOverviewMapper;
import com.xfjr.statistics.mapper.StatRiskMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 统计分析（FR-128）：经营/风险看板查询、报表导出、T+1 聚合推送。
 * 看板展示与报表导出读同一份聚合数据，保证导出与页面一致（AC3）。
 */
@Service
public class DashboardService {

    private final StatDailyOverviewMapper overviewMapper;
    private final StatRiskMapper riskMapper;

    public DashboardService(StatDailyOverviewMapper overviewMapper, StatRiskMapper riskMapper) {
        this.overviewMapper = overviewMapper;
        this.riskMapper = riskMapper;
    }

    /** 经营看板最新快照（无数据返回 null）。 */
    public StatisticsOverviewDTO overview() {
        StatDailyOverview ov = overviewMapper.selectOne(
                new LambdaQueryWrapper<StatDailyOverview>()
                        .orderByDesc(StatDailyOverview::getStatDate)
                        .last("limit 1"));
        if (ov == null) {
            return null;
        }
        StatisticsOverviewDTO dto = new StatisticsOverviewDTO();
        BeanUtils.copyProperties(ov, dto);
        return dto;
    }

    /** 风险看板最新快照（无数据返回 null）。 */
    public StatisticsRiskDTO risk() {
        StatRisk sr = riskMapper.selectOne(
                new LambdaQueryWrapper<StatRisk>()
                        .orderByDesc(StatRisk::getStatDate)
                        .last("limit 1"));
        if (sr == null) {
            return null;
        }
        StatisticsRiskDTO dto = new StatisticsRiskDTO();
        BeanUtils.copyProperties(sr, dto);
        return dto;
    }

    /** 经营看板报表导出（CSV，含页面展示的最新数据）。 */
    public String exportOverview() {
        List<StatDailyOverview> list = overviewMapper.selectList(
                new LambdaQueryWrapper<StatDailyOverview>().orderByAsc(StatDailyOverview::getStatDate));
        StringBuilder sb = new StringBuilder(
                "statDate,outstandingBalance,pendingRepay,todayTransaction,todayTransactionCount,newUserCount,totalLendAmount\n");
        for (StatDailyOverview o : list) {
            sb.append(o.getStatDate()).append(',')
                    .append(nz(o.getOutstandingBalance())).append(',')
                    .append(nz(o.getPendingRepay())).append(',')
                    .append(nz(o.getTodayTransaction())).append(',')
                    .append(o.getTodayTransactionCount()).append(',')
                    .append(o.getNewUserCount()).append(',')
                    .append(nz(o.getTotalLendAmount())).append('\n');
        }
        return sb.toString();
    }

    /** 风险看板报表导出（CSV，含页面展示的最新数据）。 */
    public String exportRisk() {
        List<StatRisk> list = riskMapper.selectList(
                new LambdaQueryWrapper<StatRisk>().orderByAsc(StatRisk::getStatDate));
        StringBuilder sb = new StringBuilder(
                "statDate,overdueRate,migrationRate,overdueAmount,agingDistribution,fiveClassDistribution\n");
        for (StatRisk r : list) {
            sb.append(r.getStatDate()).append(',')
                    .append(nz(r.getOverdueRate())).append(',')
                    .append(nz(r.getMigrationRate())).append(',')
                    .append(nz(r.getOverdueAmount())).append(',')
                    .append(quote(r.getAgingDistribution())).append(',')
                    .append(quote(r.getFiveClassDistribution())).append('\n');
        }
        return sb.toString();
    }

    /** T+1 聚合推送：upsert 经营与风险聚合表。 */
    @Transactional(rollbackFor = Exception.class)
    public void sync(StatisticsSyncRequest req) {
        if (req.getStatDate() == null) {
            throw new IllegalArgumentException("statDate 不能为空");
        }
        LocalDateTime now = LocalDateTime.now();

        StatDailyOverview ov = overviewMapper.selectOne(
                new LambdaQueryWrapper<StatDailyOverview>().eq(StatDailyOverview::getStatDate, req.getStatDate()));
        boolean ovNew = ov == null;
        if (ovNew) {
            ov = new StatDailyOverview();
            ov.setStatDate(req.getStatDate());
            ov.setCreateDate(now);
        }
        ov.setOutstandingBalance(req.getOutstandingBalance());
        ov.setPendingRepay(req.getPendingRepay());
        ov.setTodayTransaction(req.getTodayTransaction());
        ov.setTodayTransactionCount(req.getTodayTransactionCount());
        ov.setNewUserCount(req.getNewUserCount());
        ov.setTotalLendAmount(req.getTotalLendAmount());
        ov.setUpdateDate(now);
        if (ovNew) {
            overviewMapper.insert(ov);
        } else {
            overviewMapper.updateById(ov);
        }

        StatRisk sr = riskMapper.selectOne(
                new LambdaQueryWrapper<StatRisk>().eq(StatRisk::getStatDate, req.getStatDate()));
        boolean srNew = sr == null;
        if (srNew) {
            sr = new StatRisk();
            sr.setStatDate(req.getStatDate());
            sr.setCreateDate(now);
        }
        sr.setOverdueRate(req.getOverdueRate());
        sr.setMigrationRate(req.getMigrationRate());
        sr.setOverdueAmount(req.getOverdueAmount());
        sr.setAgingDistribution(req.getAgingDistribution());
        sr.setFiveClassDistribution(req.getFiveClassDistribution());
        sr.setUpdateDate(now);
        if (srNew) {
            riskMapper.insert(sr);
        } else {
            riskMapper.updateById(sr);
        }
    }

    private String nz(java.math.BigDecimal v) {
        return v == null ? "0" : v.toPlainString();
    }

    private String quote(String s) {
        if (s == null) {
            return "";
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}

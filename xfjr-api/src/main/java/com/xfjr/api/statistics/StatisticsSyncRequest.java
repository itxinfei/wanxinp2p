package com.xfjr.api.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * 统计聚合推送请求（FR-128 T+1 管道入口）。
 * 由外部聚合任务计算后推送到本服务，upsert 到聚合表。
 */
@Data
public class StatisticsSyncRequest {
    private LocalDate statDate;

    // 经营看板
    private BigDecimal outstandingBalance;
    private BigDecimal pendingRepay;
    private BigDecimal todayTransaction;
    private Integer todayTransactionCount;
    private Integer newUserCount;
    private BigDecimal totalLendAmount;

    // 风险看板
    private BigDecimal overdueRate;
    private BigDecimal migrationRate;
    private BigDecimal overdueAmount;
    private String agingDistribution;
    private String fiveClassDistribution;
}

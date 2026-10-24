package com.xfjr.api.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 风险看板指标（FR-128）。 */
@Data
public class StatisticsRiskDTO {
    private LocalDate statDate;
    /** 逾期率（0~1） */
    private BigDecimal overdueRate;
    /** 迁徙率（0~1） */
    private BigDecimal migrationRate;
    /** 逾期金额 */
    private BigDecimal overdueAmount;
    /** 账龄分布 JSON */
    private String agingDistribution;
    /** 五级分类 JSON */
    private String fiveClassDistribution;
}

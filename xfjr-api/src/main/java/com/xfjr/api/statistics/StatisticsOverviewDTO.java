package com.xfjr.api.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 经营看板指标（FR-128）。 */
@Data
public class StatisticsOverviewDTO {
    private LocalDate statDate;
    /** 在贷余额 */
    private BigDecimal outstandingBalance;
    /** 待还金额 */
    private BigDecimal pendingRepay;
    /** 当日交易额 */
    private BigDecimal todayTransaction;
    /** 当日交易笔数 */
    private Integer todayTransactionCount;
    /** 当日新增用户数 */
    private Integer newUserCount;
    /** 累计出借金额 */
    private BigDecimal totalLendAmount;
}

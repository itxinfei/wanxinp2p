package com.xfjr.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营看板日聚合（对齐 p2p_statistics.stat_daily_overview，T+1）。
 */
@Data
@TableName("stat_daily_overview")
public class StatDailyOverview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 统计日期 */
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
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

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
 * 风险看板日聚合（对齐 p2p_statistics.stat_risk，T+1）。
 * 账龄分布 / 五级分类以 JSON 存储，结构与看板展示一致（AC3 导出同源）。
 */
@Data
@TableName("stat_risk")
public class StatRisk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 统计日期 */
    private LocalDate statDate;
    /** 逾期率（0~1） */
    private BigDecimal overdueRate;
    /** 迁徙率（0~1） */
    private BigDecimal migrationRate;
    /** 逾期金额 */
    private BigDecimal overdueAmount;
    /** 账龄分布 JSON，如 {"1-30":10,"31-60":5,"61-90":2,"91-180":1,"180+":0} */
    private String agingDistribution;
    /** 五级分类 JSON，如 {"正常":90,"关注":6,"次级":2,"可疑":1,"损失":1} */
    private String fiveClassDistribution;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

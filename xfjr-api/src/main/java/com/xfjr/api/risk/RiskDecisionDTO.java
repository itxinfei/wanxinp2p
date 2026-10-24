package com.xfjr.api.risk;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 风控决策结果 / 决策记录。
 */
@Data
public class RiskDecisionDTO {
    private String bizNo;
    private String bizType;
    private String consumerNo;
    /** 结论：PASS-通过 REJECT-拒绝 REVIEW-人工 */
    private String decision;
    /** 命中规则/名单编码（逗号分隔） */
    private String ruleHits;
    /** 信用评分（0~100） */
    private Integer score;
    /** 决策详情(JSON) */
    private String detail;
    private LocalDateTime createDate;
}

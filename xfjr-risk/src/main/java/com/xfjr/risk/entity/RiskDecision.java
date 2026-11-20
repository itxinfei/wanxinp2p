package com.xfjr.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风控决策记录（对齐 p2p_risk.risk_decision 表），按 bizNo 可追溯完整决策链路。
 */
@TableName("risk_decision")
public class RiskDecision implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private String bizType;
    private String consumerNo;
    private String ruleHits;
    /** PASS-通过 REJECT-拒绝 REVIEW-人工 */
    private String decision;
    private Integer score;
    private String detail;
    private LocalDateTime createDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getConsumerNo() { return consumerNo; }
    public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }
    public String getRuleHits() { return ruleHits; }
    public void setRuleHits(String ruleHits) { this.ruleHits = ruleHits; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
}

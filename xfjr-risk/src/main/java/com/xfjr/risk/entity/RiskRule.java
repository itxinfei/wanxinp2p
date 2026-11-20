package com.xfjr.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风控规则（对齐 p2p_risk.risk_rule 表）。
 * rule_type: ANTI_FRAUD-反欺诈 CREDIT_SCORE-信用评分 LIST-名单 QUOTA-额度
 * expression 为 JSON 配置，决策引擎按此对请求字段做条件判断。
 */
@TableName("risk_rule")
public class RiskRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String expression;
    private Integer riskWeight;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    public Integer getRiskWeight() { return riskWeight; }
    public void setRiskWeight(Integer riskWeight) { this.riskWeight = riskWeight; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}

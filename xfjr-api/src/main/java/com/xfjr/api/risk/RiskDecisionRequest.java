package com.xfjr.api.risk;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 风控决策请求（内部调用，发标/开户/提现等交易前触发）。
 * 字段为可选：名单与规则引擎按实际提供的维度/字段进行匹配与评分。
 */
@Data
public class RiskDecisionRequest {
    /** 业务流水号，决策留痕与幂等的唯一键（必填） */
    private String bizNo;
    /** 业务类型：REGISTER/OPEN_ACCOUNT/RECHARGE/WITHDRAW/ISSUE_LOAN 等 */
    private String bizType;
    /** 用户编号 */
    private String consumerNo;
    /** 手机号（MOBILE 维度） */
    private String mobile;
    /** 身份证号（ID_CARD 维度） */
    private String idCard;
    /** 设备指纹（DEVICE 维度） */
    private String device;
    /** 来源 IP（IP 维度） */
    private String ip;
    /** 申请金额（用于额度类规则） */
    private BigDecimal applyAmount;
}

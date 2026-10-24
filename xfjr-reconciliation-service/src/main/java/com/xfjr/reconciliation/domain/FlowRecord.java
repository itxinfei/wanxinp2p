package com.xfjr.reconciliation.domain;

import java.math.BigDecimal;

/**
 * 对账流水记录（标准化后的平台/银行单边流水）
 */
public class FlowRecord {
    private String bizNo;
    private BigDecimal amount;

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

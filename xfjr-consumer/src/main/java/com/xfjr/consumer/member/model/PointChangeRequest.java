package com.xfjr.consumer.member.model;

import lombok.Data;

/** 积分变动请求（获取/消耗通用）。 */
@Data
public class PointChangeRequest {
    private String consumerNo;
    /** 业务类型：LOAN/REPAY/INVITE/DEDUCT/EXCHANGE */
    private String bizType;
    /** 积分变动量（正数） */
    private Long amount;
    /** 业务流水号（幂等键） */
    private String bizNo;
    private String reason;
}

package com.xfjr.consumer.member.model;

import java.math.BigDecimal;
import lombok.Data;

/** 出借完成请求（触发累计出借+自动升级+发放积分）。 */
@Data
public class LendRequest {
    /** 本次出借金额 */
    private BigDecimal lendAmount;
    /** 业务流水号（幂等键） */
    private String bizNo;
}

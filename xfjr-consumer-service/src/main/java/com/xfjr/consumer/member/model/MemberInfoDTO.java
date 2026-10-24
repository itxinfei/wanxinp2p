package com.xfjr.consumer.member.model;

import java.math.BigDecimal;
import lombok.Data;

/** 用户会员档案。 */
@Data
public class MemberInfoDTO {
    private String consumerNo;
    private String currentLevel;
    private BigDecimal cumulativeLendAmount;
    private Integer activeScore;
}

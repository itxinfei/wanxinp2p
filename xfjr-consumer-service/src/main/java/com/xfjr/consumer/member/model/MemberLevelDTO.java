package com.xfjr.consumer.member.model;

import java.math.BigDecimal;
import lombok.Data;

/** 会员等级配置。 */
@Data
public class MemberLevelDTO {
    private String levelCode;
    private String levelName;
    private Integer levelNo;
    private BigDecimal minLendAmount;
    private String privilege;
}

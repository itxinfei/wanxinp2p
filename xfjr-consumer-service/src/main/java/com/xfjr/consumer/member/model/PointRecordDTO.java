package com.xfjr.consumer.member.model;

import java.time.LocalDateTime;
import lombok.Data;

/** 积分流水记录。 */
@Data
public class PointRecordDTO {
    private Long id;
    private String consumerNo;
    private String pointType;
    private String bizType;
    private Long amount;
    private Long balanceAfter;
    private String bizNo;
    private String reason;
    private LocalDateTime createDate;
}

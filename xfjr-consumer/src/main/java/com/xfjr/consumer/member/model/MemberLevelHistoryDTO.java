package com.xfjr.consumer.member.model;

import java.time.LocalDateTime;
import lombok.Data;

/** 等级变更记录。 */
@Data
public class MemberLevelHistoryDTO {
    private String consumerNo;
    private String fromLevel;
    private String toLevel;
    private String changeReason;
    private LocalDateTime createDate;
}

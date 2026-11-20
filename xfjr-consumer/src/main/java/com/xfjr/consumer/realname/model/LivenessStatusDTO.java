package com.xfjr.consumer.realname.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 实名活体核验状态（FR-135，含最新分数与结论，可追溯）。 */
@Data
public class LivenessStatusDTO {
    private String consumerNo;
    /** 结论：PASSED/REVIEW/REJECTED/UNVERIFIED */
    private String status;
    private BigDecimal livenessScore;
    private BigDecimal faceMatchScore;
    private Integer livenessPassed;
    private Integer riskFlag;
    private String attackType;
    private Integer retryCount;
    private String message;
    private LocalDateTime createDate;
}

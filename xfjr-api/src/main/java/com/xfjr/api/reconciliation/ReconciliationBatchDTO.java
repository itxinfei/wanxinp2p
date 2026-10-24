package com.xfjr.api.reconciliation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReconciliationBatchDTO {
    private String batchNo;
    private String bizDate;
    private String status;
    private BigDecimal totalPlatform;
    private BigDecimal totalBank;
    private Integer matchedCount;
    private Integer diffCount;
    private LocalDateTime createDate;
}

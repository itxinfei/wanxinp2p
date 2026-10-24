package com.xfjr.api.reconciliation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReconciliationDiffDTO {
    private Long id;
    private String batchNo;
    private String bizNo;
    private String diffType;
    private BigDecimal platformAmount;
    private BigDecimal bankAmount;
    private String status;
    private String handler;
    private LocalDateTime handleTime;
}

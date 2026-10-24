package com.xfjr.api.depository.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 存管指令状态（FR-134）。 */
@Data
public class DepositoryCommandStatusDTO {
    private String requestNo;
    private String serviceName;
    private String serviceCode;
    private String direction;
    private BigDecimal amount;
    /** INIT/PROCESSING/SUCCESS/FAIL/CANCELED */
    private String status;
    private String requestPayload;
    private String responsePayload;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

package com.xfjr.api.contract;

import java.time.LocalDateTime;
import lombok.Data;

/** 合同签署记录（FR-133 AC2 证据链）。 */
@Data
public class ContractSignDTO {
    private Long id;
    private String contractNo;
    /** 签署人 */
    private String signer;
    /** 签署方式 */
    private String signType;
    /** 证书序列号 */
    private String certSerial;
    private LocalDateTime signTime;
}

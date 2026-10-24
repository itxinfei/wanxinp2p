package com.xfjr.api.contract;

import java.time.LocalDateTime;
import lombok.Data;

/** 合同存证记录（FR-133）。 */
@Data
public class ContractEvidenceDTO {
    private Long id;
    private String contractNo;
    /** 存证类型：THIRD_PARTY-第三方存证 CHAIN-区块链存证 DOUBLE_RECORD-双录 */
    private String evidenceType;
    /** 存证编号 */
    private String evidenceNo;
    /** 存证存储地址 */
    private String storageUrl;
    private LocalDateTime createDate;
}

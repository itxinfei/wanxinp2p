package com.xfjr.api.contract;

import java.util.List;
import lombok.Data;

/** 生成合同请求（放款成功时触发，FR-133 AC1）。 */
@Data
public class CreateContractRequest {
    private Long projectId;
    /** 借款人用户标识 */
    private Long borrowerId;
    /** 出借人用户标识列表 */
    private List<Long> lenderIds;
    /** 合同类型：LOAN / BROKERAGE（默认 LOAN） */
    private String contractType;
}

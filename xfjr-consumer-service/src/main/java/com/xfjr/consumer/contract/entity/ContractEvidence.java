package com.xfjr.consumer.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 合同存证记录（FR-133，对齐 p2p_consumer.contract_evidence）。
 */
@Data
@TableName("contract_evidence")
public class ContractEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    /** THIRD_PARTY-第三方存证 CHAIN-区块链存证 DOUBLE_RECORD-双录 */
    private String evidenceType;
    /** 存证编号 */
    private String evidenceNo;
    /** 存证存储地址 */
    private String storageUrl;
    private LocalDateTime createDate;
}

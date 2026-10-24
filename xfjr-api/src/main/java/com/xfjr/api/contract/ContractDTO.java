package com.xfjr.api.contract;

import java.time.LocalDateTime;
import lombok.Data;

/** 电子合同（FR-133）。 */
@Data
public class ContractDTO {
    private Long id;
    /** 合同编号 */
    private String contractNo;
    /** 合同类型：LOAN-借款合同 BROKERAGE-居间服务协议 */
    private String type;
    private Long projectId;
    private Long lenderId;
    private Long borrowerId;
    private Long templateId;
    /** 文件服务存储标识 */
    private String fileId;
    /** 文件哈希(SHA-256)，用于防篡改校验(AC3) */
    private String fileHash;
    /** 渲染后合同内容 */
    private String content;
    /** 状态：GENERATED/SIGNED/EVIDENCED/ARCHIVED/EXPIRED */
    private String status;
    private LocalDateTime signTime;
    private LocalDateTime createDate;
}

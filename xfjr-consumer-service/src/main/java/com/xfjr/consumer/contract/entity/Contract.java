package com.xfjr.consumer.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 电子合同（FR-133，对齐 p2p_consumer.contract）。
 * fileHash 为渲染内容 SHA-256，用于防篡改校验（AC3）。
 */
@Data
@TableName("contract")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 合同编号 */
    private String contractNo;
    /** LOAN-借款合同 BROKERAGE-居间服务协议 */
    private String type;
    private Long projectId;
    private Long lenderId;
    private Long borrowerId;
    private Long templateId;
    /** 文件服务存储标识 */
    private String fileId;
    /** 文件哈希 SHA-256 */
    private String fileHash;
    /** 渲染后合同内容 */
    private String content;
    /** GENERATED/SIGNED/EVIDENCED/ARCHIVED/EXPIRED */
    private String status;
    private LocalDateTime signTime;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

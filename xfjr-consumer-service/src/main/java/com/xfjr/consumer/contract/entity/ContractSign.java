package com.xfjr.consumer.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 合同签署记录（FR-133 AC2，对齐 p2p_consumer.contract_sign）。
 */
@Data
@TableName("contract_sign")
public class ContractSign implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    /** 签署人 */
    private String signer;
    /** 签署方式 */
    private String signType;
    /** 证书序列号 */
    private String certSerial;
    private LocalDateTime signTime;
    private LocalDateTime createDate;
}

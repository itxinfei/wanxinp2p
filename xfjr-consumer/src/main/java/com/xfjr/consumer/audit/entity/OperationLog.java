package com.xfjr.consumer.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志/审计（FR-132）。追加写 + 哈希链，不可篡改（AC2）。
 * curHash = SHA-256(prevHash | operator | action | target | result | ip | createDate)
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作人 */
    private String operator;
    /** 动作类型（资金操作/审核/权限变更/登录/登出） */
    private String action;
    /** 操作对象 */
    private String target;
    /** 结果 SUCCESS/FAIL */
    private String result;
    /** 来源 IP */
    private String ip;
    /** 上一条 curHash（创世为 GENESIS） */
    private String prevHash;
    /** 本条哈希 */
    private String curHash;
    private LocalDateTime createDate;
}

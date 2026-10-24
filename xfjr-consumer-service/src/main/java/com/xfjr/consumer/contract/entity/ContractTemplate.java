package com.xfjr.consumer.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 合同模板（FR-133“按模板生成”）。
 */
@Data
@TableName("contract_template")
public class ContractTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 模板编号 */
    private String templateNo;
    private String templateName;
    /** 适用合同类型：LOAN / BROKERAGE */
    private String contractType;
    /** 模板内容（含 ${} 占位符） */
    private String templateContent;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

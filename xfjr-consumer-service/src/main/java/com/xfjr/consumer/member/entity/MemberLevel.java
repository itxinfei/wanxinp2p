package com.xfjr.consumer.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 会员等级配置（对齐 p2p_consumer.member_level），承载等级门槛与权益配置。
 */
@Data
@TableName("member_level")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 等级编码：BRONZE/SILVER/GOLD/PLATINUM */
    private String levelCode;
    private String levelName;
    /** 等级序号，越大越高 */
    private Integer levelNo;
    /** 累计出借金额门槛 */
    private BigDecimal minLendAmount;
    /** 权益描述 */
    private String privilege;
    /** 状态 1启用 0停用 */
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

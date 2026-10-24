package com.xfjr.consumer.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分流水（对齐 p2p_consumer.member_point_record），支撑 FR-127 AC2/AC3。
 * 余额 = 获取(EARN)合计 - 消耗(CONSUME)合计；balanceAfter 记录变动后余额便于展示。
 */
@Data
@TableName("member_point_record")
public class MemberPointRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String consumerNo;
    /** EARN-获取 CONSUME-消耗 */
    private String pointType;
    /** LOAN-出借 REPAY-还款 INVITE-邀请 DEDUCT-抵扣 EXCHANGE-兑换 */
    private String bizType;
    /** 积分变动量（正数） */
    private Long amount;
    /** 变动后余额 */
    private Long balanceAfter;
    /** 业务流水号（幂等键） */
    private String bizNo;
    private String reason;
    private LocalDateTime createDate;
}

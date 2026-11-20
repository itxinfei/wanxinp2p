package com.xfjr.consumer.realname.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户实名信息（FR-135，对齐 p2p_consumer.consumer_realname）。
 * 新增活体/人脸字段；仅存分数与结果，不存原始人脸图像（AC4）。
 */
@Data
@TableName("consumer_realname")
public class ConsumerRealname implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String consumerNo;
    /** 姓名 */
    private String realName;
    /** 身份证号 */
    private String idCard;
    /** 实名状态：UNVERIFIED/VERIFIED/REVIEW/REJECTED */
    private String status;
    /** 活体检测分数(0~1) */
    private BigDecimal livenessScore;
    /** 人脸比对分数(0~1) */
    private BigDecimal faceMatchScore;
    /** 活体是否通过 1是 0否 */
    private Integer livenessPassed;
    /** 风险标记 1是 0否（AC2 翻拍/照片攻击） */
    private Integer riskFlag;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}

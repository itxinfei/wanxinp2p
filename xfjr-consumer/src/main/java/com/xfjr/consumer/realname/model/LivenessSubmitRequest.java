package com.xfjr.consumer.realname.model;

import lombok.Data;

/** 提交活体/人脸核验请求（FR-135）。图像仅传 xfjr-file 引用，不承载原始人脸数据（AC4）。 */
@Data
public class LivenessSubmitRequest {
    private String consumerNo;
    /** OCR 识别姓名 */
    private String realName;
    /** OCR 识别身份证号 */
    private String idCard;
    /** 活体采集件引用 */
    private String livenessImageRef;
    /** 人脸比对件引用 */
    private String faceImageRef;
    /** 挑战类型：ACTION/TEXT/LIGHT */
    private String challengeType;
}

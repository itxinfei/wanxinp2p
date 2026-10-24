package com.xfjr.consumer.realname.verify;

import lombok.Data;

/** 活体/人脸核验请求（提交给算法服务）。图像仅传文件服务引用，不在此承载原始人脸数据（AC4）。 */
@Data
public class LivenessVerifyRequest {
    private String consumerNo;
    /** OCR 识别姓名 */
    private String realName;
    /** OCR 识别身份证号 */
    private String idCard;
    /** 活体采集件在 file-service 的引用 */
    private String livenessImageRef;
    /** 人脸比对件在 file-service 的引用 */
    private String faceImageRef;
    /** 挑战类型：ACTION/TEXT/LIGHT */
    private String challengeType;
}

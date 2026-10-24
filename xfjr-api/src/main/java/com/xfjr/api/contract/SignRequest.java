package com.xfjr.api.contract;

import lombok.Data;

/** 合同签署请求（FR-133 AC2）。 */
@Data
public class SignRequest {
    /** 签署人 */
    private String signer;
    /** 签署方式：SMS_VERIFY/CA/IMAGE（身份核验+短信验证+意愿确认） */
    private String signType;
    /** 证书序列号 */
    private String certSerial;
}

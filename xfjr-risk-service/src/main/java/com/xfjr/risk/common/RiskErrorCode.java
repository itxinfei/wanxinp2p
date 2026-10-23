package com.xfjr.risk.common;

import com.xfjr.common.domain.ErrorCode;

public enum RiskErrorCode implements ErrorCode {
    E_180101(180101, "业务处理失败"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private RiskErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RiskErrorCode setErrorCode(int code) {
        for (RiskErrorCode errorCode : RiskErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}

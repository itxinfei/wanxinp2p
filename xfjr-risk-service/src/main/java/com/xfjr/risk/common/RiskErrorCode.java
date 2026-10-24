package com.xfjr.risk.common;

import com.xfjr.common.domain.ErrorCode;

public enum RiskErrorCode implements ErrorCode {
    E_180101(180101, "业务处理失败"),
    E_180201(180201, "风控决策请求参数缺失"),
    E_180202(180202, "风控决策记录不存在"),
    E_180301(180301, "名单参数非法"),
    E_180302(180302, "名单记录不存在"),
    E_180303(180303, "名单维度不支持"),
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

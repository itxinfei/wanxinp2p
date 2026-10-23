package com.xfjr.reconciliation.common;

import com.xfjr.common.domain.ErrorCode;

public enum ReconciliationErrorCode implements ErrorCode {
    E_110101(110101, "业务处理失败"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private ReconciliationErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReconciliationErrorCode setErrorCode(int code) {
        for (ReconciliationErrorCode errorCode : ReconciliationErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}

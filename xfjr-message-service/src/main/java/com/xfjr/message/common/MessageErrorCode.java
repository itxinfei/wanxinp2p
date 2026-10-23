package com.xfjr.message.common;

import com.xfjr.common.domain.ErrorCode;

public enum MessageErrorCode implements ErrorCode {
    E_120201(120201, "业务处理失败"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private MessageErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageErrorCode setErrorCode(int code) {
        for (MessageErrorCode errorCode : MessageErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}

package com.xfjr.file.common;

import com.xfjr.common.domain.ErrorCode;

public enum FileErrorCode implements ErrorCode {
    E_120101(120101, "业务处理失败"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private FileErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FileErrorCode setErrorCode(int code) {
        for (FileErrorCode errorCode : FileErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}

package com.xfjr.file.common;

import com.xfjr.common.domain.ErrorCode;

public enum FileErrorCode implements ErrorCode {
    E_FILE_120101(120101, "文件类型不支持"),
    E_FILE_120102(120102, "文件大小超过上限"),
    E_FILE_120103(120103, "文件不存在"),
    E_FILE_120104(120104, "文件已删除或过期"),
    E_FILE_120105(120105, "文件存储失败"),
    ;

    private final int code;
    private final String desc;

    FileErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}

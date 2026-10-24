package com.xfjr.message.common;

import com.xfjr.common.domain.ErrorCode;

/**
 * 消息服务业务错误码（1202xx 区段）
 */
public enum MessageErrorCode implements ErrorCode {

    E_MESSAGE_120201(120201, "消息渠道不支持"),
    E_MESSAGE_120202(120202, "消息发送过于频繁"),
    E_MESSAGE_120203(120203, "消息模板不存在"),
    E_MESSAGE_120204(120204, "消息标题或内容不能为空"),
    ;

    private final int code;
    private final String desc;

    MessageErrorCode(int code, String desc) {
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

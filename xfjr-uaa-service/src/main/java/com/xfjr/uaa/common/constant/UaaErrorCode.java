package com.xfjr.uaa.common.constant;

import com.xfjr.common.domain.ErrorCode;

/**
 * 统一认证服务（uaa）异常编码 15。
 * 前两位:服务标识(15=uaa)  中间两位:模块标识  后两位:异常标识
 */
public enum UaaErrorCode implements ErrorCode {
    E_150101(150101, "短信验证码错误或已过期"),
    E_150102(150102, "短信验证码尝试次数过多已失效"),
    E_150201(150201, "令牌无效或已吊销"),
    E_150301(150301, "用户已被封禁"),
    E_150401(150401, "参数非法"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private UaaErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UaaErrorCode setErrorCode(int code) {
        for (UaaErrorCode errorCode : UaaErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}

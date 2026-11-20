package com.xfjr.reconciliation.common;

import com.xfjr.common.domain.ErrorCode;

public enum ReconciliationErrorCode implements ErrorCode {
    E_RECON_110101(110101, "对账处理失败"),
    E_RECON_110102(110102, "账单文件为空或格式错误"),
    ;

    private final int code;
    private final String desc;

    ReconciliationErrorCode(int code, String desc) {
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

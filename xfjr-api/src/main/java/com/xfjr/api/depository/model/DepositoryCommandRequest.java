package com.xfjr.api.depository.model;

import java.math.BigDecimal;
import lombok.Data;

/** 存管指令下发请求（FR-134，业务服务 → 存管代理，内部调用）。 */
@Data
public class DepositoryCommandRequest {
    /** 请求流水号（幂等键，缺省则生成） */
    private String requestNo;
    /** 存管接口名 */
    private String serviceName;
    /** 业务服务标识（回调 params.serviceName 区分业务） */
    private String serviceCode;
    /** 指令方向 IN/OUT */
    private String direction;
    /** 金额 */
    private BigDecimal amount;
    /** 业务流水号 */
    private String bizNo;
    /** 业务数据报文（将被签名下发） */
    private String payload;
    /** 异步回调地址（可选） */
    private String callbackUrl;
}

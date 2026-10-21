package com.xinfeip2p.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * <P>
 * 银行存管系统返回str, 转换的json对象
 * </p>
 */
@Data
public class DepositoryResponseDTO<T> implements Serializable {
    /**
     * 业务数据报文，JSON格式
     */
    @Schema(description = "业务数据报文，JSON格式")
    private T respData;
    /**
     * 签名
     */
    @Schema(description = "签名数据")
    private String signature;

}

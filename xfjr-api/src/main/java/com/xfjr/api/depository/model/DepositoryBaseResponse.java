package com.xfjr.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <P>
 * 返回参数信息基类
 * </p>
 */
@Data
@Accessors(chain = true)
public class DepositoryBaseResponse implements Serializable {

    @Schema(description = "返回码")
    private String respCode;

    @Schema(description = "描述信息")
    private String respMsg;

    @Schema(description = "交易状态")
    private Integer status;

    @Schema(description = "请求流水号")
    private String requestNo;

}

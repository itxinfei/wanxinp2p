package com.xinfeip2p.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 开户返回参数信息
 * </p>
 */
@Data
public class DepositoryConsumerResponse extends DepositoryBaseResponse {

	@Schema(description = "银行代码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;
}

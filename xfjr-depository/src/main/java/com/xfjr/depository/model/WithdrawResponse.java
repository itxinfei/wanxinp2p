package com.xfjr.depository.model;

import com.xfjr.depository.common.constant.TransactionStatusCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "WithdrawResponse", description = "用户提现请求返回信息")
public class WithdrawResponse extends BaseResponse {

	@Schema(description = "交易状态")
	private String transactionStatus;

	@Override
	public void setSuccess() {
		super.setSuccess();
		this.transactionStatus = TransactionStatusCode.SUCCESS.getCode();
	}

	@Override
	public void setFailure() {
		super.setFailure();
		this.transactionStatus = TransactionStatusCode.FAIL.getCode();
	}
}

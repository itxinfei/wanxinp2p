package com.xfjr.api.depository;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 与银行存管系统对接使用的签名请求数据
 */
@Data
@Schema(name = "GatewayRequest", description = "与银行存管系统对接使用的签名请求数据")
public class GatewayRequest {

	@Schema(description = "请求的存管接口名")
	private String serviceName;

	@Schema(description = "平台编号，平台与存管系统签约时获取")
	private String platformNo;

	@Schema(description = "业务数据报文，json格式")
	private String reqData;

	@Schema(description = "对reqData参数的签名")
	private String signature;

	@Schema(description = "银行存管系统地址")
	private String depositoryUrl;


}

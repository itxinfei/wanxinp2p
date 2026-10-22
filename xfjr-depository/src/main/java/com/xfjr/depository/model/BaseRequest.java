package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 请求参数信息基类
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-23
 */
@Data
public class BaseRequest {

	@Schema(description = "请求业务流水号")
	private String requestNo;

	@Schema(description = "应用编码")
	private String appCode;
}

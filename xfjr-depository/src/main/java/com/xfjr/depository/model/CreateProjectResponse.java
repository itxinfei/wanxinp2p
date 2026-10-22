package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 创建标的返回数据
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-23
 */
@Data
@Schema(name = "CreateProjectResponse", description = "创建标的返回数据")
public class CreateProjectResponse extends BaseResponse {
	public CreateProjectResponse(String requestNo) {
		super(requestNo);
	}
}

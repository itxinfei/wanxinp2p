package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 更新标的状态请求信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-24
 */
@Data
@Schema(name = "ModifyProjectRequest", description = "更新标的状态请求信息")
public class ModifyProjectRequest extends BaseRequest{

	@Schema(description = "标的号")
	private String projectNo;

	@Schema(description = "更新标的状态")
	private String projectStatus;
}

package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 更新标的状态返回信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-24
 */
@Data
@Schema(name = "ModifyProjectResponse", description = "更新标的状态返回信息")
public class ModifyProjectResponse extends BaseResponse {

	@Schema(description = "标的状态")
	private String projectStatus;

	public ModifyProjectResponse(String requestNo) {
		super(requestNo);
	}
}

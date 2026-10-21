package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 预授权处理返回信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-24
 */
@Data
@Schema(name = "UserAutoPreTransactionResponse", description = "预授权处理返回信息")
public class UserAutoPreTransactionResponse extends BaseResponse {

	@Schema(description = "预处理业务类型")
	private String bizType;
}

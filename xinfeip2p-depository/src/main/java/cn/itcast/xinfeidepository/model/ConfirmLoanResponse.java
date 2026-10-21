package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 放款确认返回信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-28
 */
@Data
@Schema(name = "ConfirmLoanResponse", description = "放款确认返回信息")
public class ConfirmLoanResponse extends BaseResponse {

}

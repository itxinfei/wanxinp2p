package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <P>
 * 标的满标放款确认信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019/5/28
 */
@Data
@Schema(name = "ConfirmLoanRequest", description = "标的满标放款确认信息")
public class ConfirmLoanRequest extends BaseRequest {

	@Schema(description = "标的编码")
	private String projectNo;

	@Schema(description = "平台佣金")
    private BigDecimal commission;

	@Schema(description = "放款明细")
	private List<ConfirmLoanDetailRequest> details;

}

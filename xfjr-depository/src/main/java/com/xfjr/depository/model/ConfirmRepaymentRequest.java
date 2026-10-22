package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <P>
 * 还款确认请求信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019/5/28
 */
@Data
@Schema(name = "ConfirmRepaymentRequest", description = "还款确认请求信息")
public class ConfirmRepaymentRequest extends BaseRequest {

    @Schema(description = "预处理业务流水号")
    private String preRequestNo;

    @Schema(description = "标的编码")
    private String projectNo;

    @Schema(description = "还款总额")
	private BigDecimal amount;

    @Schema(description = "平台佣金")
    private BigDecimal commission;

    @Schema(description = "放款明细")
    private List<ConfirmRepaymentDetailRequest> details;

}

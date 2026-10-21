package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 还款明细请求信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019/5/28
 */
@Data
@Schema(name = "ConfirmRepaymentDetailRequest", description = "还款明细请求信息")
public class ConfirmRepaymentDetailRequest {

    @Schema(description = "投资人用户编码")
    private String userNo;

    @Schema(description = "向投资人收取的佣金")
    private BigDecimal commission;

    @Schema(description = "派息")
    private BigDecimal dividend;

    @Schema(description = "投资人应得本金")
    private BigDecimal amount;

    @Schema(description = "投资人应得利息")
    private BigDecimal interest;
}
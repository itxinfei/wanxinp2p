package com.xinfeip2p.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <P>
 * 还款信息
 * </p>
 *
 * @author wuzhao@itcast.cn
 * @since 2019/5/26
 */
@Data
@Schema(name = "RepaymentRequest", description = "还款请求信息")
public class RepaymentRequest {

    @Schema(description = "请求流水号")
    private String requestNo;

    @Schema(description = "预处理业务流水号")
    private String preRequestNo;

    @Schema(description = "标的编码")
    private String projectNo;

    @Schema(description = "平台佣金 -- 具体金额")
    private BigDecimal commission;

    @Schema(description = "放款明细")
    private List<RepaymentDetailRequest> details;

    @Schema(description = "业务id")
    private Long id;

    @Schema(description = "还款总额")
    private BigDecimal amount;
}

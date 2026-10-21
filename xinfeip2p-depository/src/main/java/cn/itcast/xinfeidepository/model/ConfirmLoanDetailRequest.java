package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 放款明细信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019/5/28
 */
@Data
@Schema(name = "ConfirmLoanDetailRequest", description = "放款明细信息")
public class ConfirmLoanDetailRequest {

    @Schema(description = "放款金额")
    private BigDecimal amount;


	@Schema(description = "预处理业务流水号")
	private String preRequestNo;
}

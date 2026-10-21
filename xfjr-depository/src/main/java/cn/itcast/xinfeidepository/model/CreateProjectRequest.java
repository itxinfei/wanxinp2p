package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 创建标的请求数据
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019/5/20
 */
@Data
@Schema(name = "CreateProjectRequest", description = "创建标的请求数据")
public class CreateProjectRequest extends BaseRequest {

	@Schema(description = "平台用户编号")
    private String userNo;

	@Schema(description = "标的号")
    private String projectNo;

	@Schema(description = "标的金额")
    private BigDecimal projectAmount;

	@Schema(description = "标的名称")
    private String projectName;

	@Schema(description = "标的描述")
    private String projectDesc;

	@Schema(description = "标的产品类型")
    private String projectType;

	@Schema(description = "标的期限")
    private Integer projectPeriod;

	@Schema(description = "年化利率( 5%只传5 )")
    private BigDecimal annualRate;

	@Schema(description = "还款方式")
    private String repaymentWay;

	@Schema(description = "是否是债权出让标")
	private Integer isAssignment;
}

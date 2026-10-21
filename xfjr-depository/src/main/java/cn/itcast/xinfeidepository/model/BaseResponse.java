package cn.itcast.xinfeidepository.model;

import cn.itcast.xinfeidepository.common.constant.StatusCode;
import cn.itcast.xinfeidepository.common.domain.LocalReturnCode;
import cn.itcast.xinfeidepository.common.domain.RemoteReturnCode;
import cn.itcast.xinfeidepository.common.domain.ReturnCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 返回参数信息基类
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-23
 */
@Data
public class BaseResponse {

	@Schema(description = "返回码")
	private String respCode;

	@Schema(description = "描述信息")
	private String respMsg;

	@Schema(description = "交易状态")
	private Integer status;

	@Schema(description = "请求流水号")
	private String requestNo;

	public BaseResponse() {
		this("", LocalReturnCode.SUCCESS, StatusCode.STATUS_IN.getCode());
	}

	public BaseResponse(String requestNo) {
		this(requestNo, LocalReturnCode.SUCCESS, StatusCode.STATUS_IN.getCode());
	}

	public BaseResponse(String code, String msg) {
		this.respCode = code;
		this.respMsg = msg;
	}

	public BaseResponse(ReturnCode returnCode) {
		this.respCode = returnCode.getCode();
		this.respMsg = returnCode.getDesc();
	}

	public BaseResponse(String requestNo, ReturnCode returnCode) {
		this(requestNo, returnCode, StatusCode.STATUS_IN.getCode());
	}

	public BaseResponse(String requestNo, ReturnCode returnCode, Integer status) {
		this.requestNo = requestNo;
		this.respCode = returnCode.getCode();
		this.respMsg = returnCode.getDesc();
		this.status = status;
	}

	/*public BaseResponse(String requestNo, String code, String msg, Integer status) {
		this.requestNo = requestNo;
		this.respCode = code;
		this.respMsg = msg;
		this.status = status;
	}*/

	public void setResp(ReturnCode returnCode) {
		this.setRespCode(returnCode.getCode());
		this.setRespMsg(returnCode.getDesc());
	}

	public void setSuccess() {
		this.setResp(LocalReturnCode.SUCCESS);
		this.setStatus(StatusCode.STATUS_IN.getCode());
	}

	public void setFailure() {
		this.setResp(LocalReturnCode.EXCEPTION);
		this.setStatus(StatusCode.STATUS_FAIL.getCode());
	}

}

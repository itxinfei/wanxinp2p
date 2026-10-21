package cn.itcast.xinfeidepository.controller;

import cn.itcast.xinfeidepository.common.constant.PreTransactionCode;
import cn.itcast.xinfeidepository.common.util.EncryptUtil;
import cn.itcast.xinfeidepository.model.*;
import cn.itcast.xinfeidepository.service.BalanceDetailsService;
import cn.itcast.xinfeidepository.service.ProjectService;
import cn.itcast.xinfeidepository.service.TenderService;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直连接口
 */
@RestController
@Tag(name = "Service", description = "银行存管系统直连接口API")
public class ServiceController {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TenderService tenderService;

	@Autowired
	private BalanceDetailsService balanceDetailsService;

	@Operation(summary = "创建标的")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@GetMapping(value = "/service", params = "serviceName=CREATE_PROJECT")
	public DepositoryResponse<CreateProjectResponse> createProject(@RequestParam String serviceName,
			@RequestParam String platformNo, @RequestParam String signature, @RequestParam String reqData) {
		CreateProjectResponse projectResponse = projectService.createProject(reqData);
		return new DepositoryResponse<>(projectResponse);
	}

	@Operation(summary = "更改标的状态")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@GetMapping(value = "/service", params = "serviceName=MODIFY_PROJECT")
	public DepositoryResponse<ModifyProjectResponse> modifyProject(@RequestParam String serviceName,
			@RequestParam String platformNo, @RequestParam String signature, @RequestParam String reqData) {
		ModifyProjectResponse projectResponse = projectService.modifyProject(reqData);
		return new DepositoryResponse<>(projectResponse);
	}

	@Operation(summary = "预授权处理")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@GetMapping(value = "/service", params = "serviceName=USER_AUTO_PRE_TRANSACTION")
	public DepositoryResponse<UserAutoPreTransactionResponse> authorizeAutoPreTransaction(
			@RequestParam String serviceName, @RequestParam String platformNo, @RequestParam String signature,
			@RequestParam String reqData) {
		UserAutoPreTransactionResponse transactionResponse = new UserAutoPreTransactionResponse();

		String decodeReqData = EncryptUtil.decodeUTF8StringBase64(reqData);
		UserAutoPreTransactionRequest request;
			request = JSON.parseObject(decodeReqData, UserAutoPreTransactionRequest.class);
			request.setAppCode(platformNo);

		//判断bizType
		if (PreTransactionCode.TENDER.getCode().equals(request.getBizType())) {
			//用户投标
			transactionResponse = tenderService.autoPreTransactionForTender(request);
		} else if (PreTransactionCode.REPAYMENT.getCode().equals(request.getBizType())) {
			//还款确认冻结金额
			balanceDetailsService.autoPreTransactionForRepayment(request);
		}
		return new DepositoryResponse<>(transactionResponse);
	}

	@Operation(summary = "预处理取消", description = "取消已冻结的预处理金额")
	@Parameters({
			@Parameter(name = "requestNo", description = "请求流水号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "preRequestNo", description = "预处理业务流水号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "amount", description = "取消金额", required = true, in = ParameterIn.QUERY)})
	@GetMapping("/l/cancel-pre-freeze")
	public DepositoryResponse<String> cancelPreFreeze(@RequestParam String requestNo, @RequestParam String preRequestNo,
			@RequestParam String amount) {
		return null;
	}

	@Operation(summary = "放款确认", description = "标的满标放款确认")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@GetMapping(value = "/service", params = "serviceName=CONFIRM_LOAN")
	public DepositoryResponse<ConfirmLoanResponse> confirmLoan(@RequestParam String serviceName,
			@RequestParam String platformNo, @RequestParam String signature, @RequestParam String reqData) {
		ConfirmLoanResponse confirmLoanResponse = tenderService.confirmLoan(reqData);
		return new DepositoryResponse<>(confirmLoanResponse);
	}

	@Operation(summary = "还款确认", description = "将还款预处理冻结的资金划转至投资人账户")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@GetMapping(value = "/service", params = "serviceName=CONFIRM_REPAYMENT")
	public DepositoryResponse<ConfirmRepaymentResponse> confirmRepayment(@RequestParam String serviceName,
			@RequestParam String platformNo, @RequestParam String signature, @RequestParam String reqData) {
		ConfirmRepaymentResponse repaymentResponse = new ConfirmRepaymentResponse();
		return new DepositoryResponse<>(repaymentResponse);
	}

}